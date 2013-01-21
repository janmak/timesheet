package com.aplana.timesheet.service;

import com.aplana.timesheet.controller.TimeSheetController;
import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.EmployeeLdapDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aplana.timesheet.util.TimeSheetConstans;

@Service("oqProgectSyncService")
public class OQProjectSyncService {

    private static final Logger logger = LoggerFactory.getLogger(OQProjectSyncService.class);
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private final StringBuffer trace = new StringBuffer();

    private static final List<String> deactivatedStatus = new ArrayList<String>() {
        {
            add("5-приостановлен");
            add("7-архив");
        }
    };
    private static final List<String> newStatus = new ArrayList<String>() {
        {
            add("1-черновик");
            add("3-активный");
        }
    };

    Pattern nameLdapPattern = Pattern.compile("CN=([^/]*)");

    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private EmployeeLdapDAO employeeLdapDAO;

    @Autowired
    private EmployeeLdapService employeeLdapService;

    @Autowired
    private TSPropertyProvider propertyProvider;

    private URL oqUrl;

    @PostConstruct
    private void Init() {
        String oqUrlLocal = propertyProvider.getOqUrl();
        if (StringUtils.isBlank(oqUrlLocal)) {
            logger.warn("OQ.url parameter not found in timesheet.properties");
        } else {
            try {
                oqUrl = new URL(oqUrlLocal);
            } catch (MalformedURLException e) {
                logger.warn("OQ.url parameter has incorrect format");
            }
        }
    }

    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void setEmployeeLdapDAO(EmployeeLdapDAO employeeLdapDAO) {
        this.employeeLdapDAO = employeeLdapDAO;
    }

    @Transactional
    public void sync() {
        trace.setLength(0);
        logger.debug("oq project sync start");
        try {
            trace.append("Начало синхронизации\n");
            projectDAO.setTrace(trace);
            if (oqUrl == null) {
                logger.warn("OQ.url not found. Synchronization impossible.");
                trace.append("Синхронизация невозможна: OQ.url не указан.");
                return;
            }
            URLConnection oqc = oqUrl.openConnection();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(oqc.getInputStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/root/projects/project");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            NamedNodeMap nodeMap;
            {
                Project project;
                trace.append("В файле синхронизации найдено: ").append(nodes.getLength()).append(" проектов\n");
                for (int i = 0; i < nodes.getLength(); i++) {
                    nodeMap = nodes.item(i).getAttributes();
                    project = new Project();

                    project.setName(nodeMap.getNamedItem("name").getNodeValue().trim());
                    project.setProjectId(nodeMap.getNamedItem("id").getNodeValue());
                    project.setStartDate(format.parse(nodeMap.getNamedItem("begining").getNodeValue()));
                    project.setEndDate(format.parse(nodeMap.getNamedItem("ending").getNodeValue()));

                    String status = nodeMap.getNamedItem("status").getNodeValue();
                    Project byProjectId = projectDAO.findByProjectId(project.getProjectId());
                    if (byProjectId != null) {
                        //проект существует
                        project.getProjectId();
                        project.setActive(byProjectId.isActive());

                    } else
                        project.setActive(newStatus.contains(status));

                    if (project.isActive()) {
                        String pmLdap = nodeMap.getNamedItem("pm").getNodeValue();

                        //APLANATS-429
                        if ((pmLdap == null) || (pmLdap.equals(""))) {
                            trace.append("Проект ").append(project.getName()).append(" пропущен, т.к. не указан руководитель проекта \n");
                            continue;
                        }

                        Employee projectLeader = this.employeeDAO.findByLdapName(pmLdap.split("/")[0]);
                        if (projectLeader != null)
                            project.setManager(projectLeader);
                        else {
                            trace.append("Проект ").append(project.getName()).append(" проигнорирован, т.к. руководитель проекта ").append(pmLdap).append(" не найден в базе ldap\n");
                            continue;
                        }
                        String hcLdap = nodeMap.getNamedItem("hc").getNodeValue();
                        projectLeader = this.employeeDAO.findByLdapName(hcLdap.split("/")[0]);
                        if (projectLeader != null) {
                            Set<Division> divisions = new TreeSet<Division>();
                            divisions.add(projectLeader.getDivision());
                            project.setDivisions(divisions);
                        }
                    }
                    projectDAO.store(project);
                }
            }
            trace.append("Синхронизация завершена\n");
        } catch (Exception e) {
            logger.error("oq project sync error: ", e);
            trace.append("Синхронизация прервана из-за ошибки: ").append(e.getMessage()).append("\n");
        }

        logger.debug("oq project sync finish");
    }

    public String getTrace() {
        return trace.toString();
    }
}
