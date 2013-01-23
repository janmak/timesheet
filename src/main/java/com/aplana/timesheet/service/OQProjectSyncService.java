package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.properties.TSPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service("oqProgectSyncService")
public class OQProjectSyncService {

    private static final Logger logger = LoggerFactory.getLogger(OQProjectSyncService.class);
    private final StringBuffer trace = new StringBuffer();

    private static final List<String> newStatus = new ArrayList<String>() {
        {
            add("1-черновик");
            add("3-активный");
        }
    };

    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    EmployeeLdapService employeeLdapService;

    @Autowired
    private TSPropertyProvider propertyProvider;

    private URL oqUrl;

    @PostConstruct
    private void Init() {
        try {
            String oqUrlLocal = propertyProvider.getOqUrl();
            if (oqUrlLocal != null && !oqUrlLocal.isEmpty())
                oqUrl = new URL(oqUrlLocal);
        } catch (MalformedURLException e) {
            logger.warn("OQ.url parameter has incorrect format");
        }
    }

    /*
     * Синхронизация проектов с аплановской системой ведения проектов
     * По адресу OQurl находится список проектов вместе с сотрудниками
     * в виде xml (файл синхронизации)
     *
     * <project id="" name="" description="" customer="" ending="" status="7-архив" pm="CN=Somebody Dmitry/CN=Users/DC=aplana/DC=com" hc="CN=Zaitsev Dmitry/CN=Users/DC=aplana/DC=com" >
     *     <workgroup>
     *         <user>Ivanov Ivan</user>
     *         <user>Smirnov Semen</user>
     *         <user>Sidorov Igor</user>
     *     </workgroup>
     * </project>
    */
    @Transactional
    public void sync() {
        trace.setLength(0);
        try {
            trace.append("Начало синхронизации\n");
            projectDAO.setTrace(trace);
            // получим список веток project из xml файла
            NodeList nodes = getOQasNodeList();
            trace.append("В файле синхронизации найдено: ").append(nodes.getLength()).append(" проектов\n");

            for (int i = 0; i < nodes.getLength(); i++) {
                createOrUpdateProject(nodes.item(i).getAttributes(), projectDAO);
            }
            trace.append("Синхронизация завершена\n");
        } catch (Exception e) {
            logger.error("oq project sync error: ", e);
            trace.append("Синхронизация прервана из-за ошибки: ").append(e.getMessage()).append("\n");
        }
    }

    public void createOrUpdateProject(NamedNodeMap nodeMap, ProjectDAO dao) {
        Project project = new Project();      // проект из БД

        // поля синхронизации
        String name      = nodeMap.getNamedItem("name").getNodeValue().trim();  // название проекта
        String idProject = nodeMap.getNamedItem("id").getNodeValue();           // id проекта
        String status    = nodeMap.getNamedItem("status").getNodeValue();       // статус проекта
        String pmLdap    = nodeMap.getNamedItem("pm").getNodeValue();           // руководитель проекта
        String hcLdap    = nodeMap.getNamedItem("hc").getNodeValue();           // hc

        // ищем в БД запись о проекте
        Project findingProject = dao.findByProjectId(idProject);
        if (findingProject == null){  // если проекта еще нет в БД
            project.setActive(newStatus.contains(status)); // установим ему новый статус
        } else {
            // если проект уже существовал - статус менять не будем
            // см. //APLANATS-408
            project.setActive(findingProject.isActive());
        }

        project.setName(name);
        project.setProjectId(idProject);

        if (project.isActive()) {
            if (!setPM(project, pmLdap)){
                return; //если не указан РП или его нет в БД, то проект не сохраняем, переходим к следующему
            }
            setDivision(project, hcLdap);  // установим подразделение пользователя
        }
        dao.store(project); // запишем в БД
    }

    private boolean setPM(Project project, String pmLdap){
        //APLANATS-429
        if ((pmLdap == null) || (pmLdap.equals(""))) {
            trace.append("Проект ").append(project.getName()).append(" пропущен, т.к. не указан руководитель проекта \n");
            return false;
        }

        Employee projectLeader = this.employeeDAO.findByLdapName(pmLdap.split("/")[0]);
        if (projectLeader == null){
            trace.append("Проект ").append(project.getName()).append(" проигнорирован, " +
                    "т.к. руководитель проекта ").append(pmLdap).append(" не найден в базе ldap\n");
            return false;
        }

        project.setManager(projectLeader);
        return true;
    }

    private void setDivision(Project project, String hcLdap){
        Employee employee = this.employeeDAO.findByLdapName(hcLdap.split("/")[0]);
        if (employee != null) {
            Set<Division> divisions = new TreeSet<Division>();
            divisions.add(employee.getDivision());
            project.setDivisions(divisions);
        }
    }

    public NodeList getOQasNodeList() throws SAXException,
            ParserConfigurationException,
            XPathExpressionException {
        NodeList result = null;
        if (oqUrl == null) {
            logger.warn("OQ.url not found. Synchronization impossible.");
            trace.append("Синхронизация невозможна: OQ.url не указан.");
            return result;
        }
        try {
            URLConnection oqc = oqUrl.openConnection();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(oqc.getInputStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/root/projects/project");
            result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (IOException e){ // обрабатываем только IOException - остальные выбрасываем наверх
            logger.error("oq project sync error: ", e);
            trace.append("Синхронизация прервана из-за ошибки ввода/вывода при попытке получить и прочитать файл " +
                    "синхронизации: ").append(e.getMessage()).append("\n");
        }

        return result;
    }

    public String getTrace() {
        return trace.toString();
    }
}
