package com.aplana.timesheet.service;

import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.util.JReportBuildError;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class JasperReportService {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportService.class);

    private static final Properties propertiesFile = new Properties();

    public static final int REPORT_PRINTTYPE_HTML = 1;
    public static final int REPORT_PRINTTYPE_XLS = 2;
    public static final int REPORT_PRINTTYPE_PDF = 3;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ServletContext context;

    private final HashMap<String, JasperReport> compiledReports = new HashMap<String, JasperReport>();

    private String toUTF8String(String s) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255 && !Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                byte[] b;
                b = Character.toString(c).getBytes("utf-8");
                for ( byte aB : b ) {
                    int k = aB;
                    if ( k < 0 ) k += 256;
                    sb.append( "%" ).append( Integer.toHexString( k ).toUpperCase() );
                }
            }
        }
        return sb.toString();
    }
    public boolean makeReport(TSJasperReport report, int printtype, HttpServletResponse response, HttpServletRequest httpServletRequest) throws JReportBuildError {

        report.checkParams();

        String reportName = report.getJRName();
        Calendar calendar=new GregorianCalendar();
        String dateNorm=new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());

        String reportNameFile=report.getJRNameFile()+" "+dateNorm;
        try {
            JasperReport jasperReport = getReport(reportName + (printtype == REPORT_PRINTTYPE_XLS ? "_xls" : ""));

            Map params = new HashMap();
            params.put(JRParameter.IS_IGNORE_PAGINATION, (printtype != REPORT_PRINTTYPE_PDF));
            params.put("reportParams", report);

            JRDataSource jrDataSource = report.prepareDataSource();
            if (jrDataSource == null) {
            	return false;
            }
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, jrDataSource);

            String suffix = "";
            String contentType = "application/octet-stream";

            switch (printtype) {
                case REPORT_PRINTTYPE_HTML:
                    suffix = ".html";
                    contentType = "text/html; charset=UTF-8";
                    break;
                case REPORT_PRINTTYPE_PDF:
                    suffix = ".pdf";
                    contentType = "application/pdf";
                    break;
                case REPORT_PRINTTYPE_XLS:
                    suffix = ".xls";
                    contentType = "application/vnd.ms-excel";
                    break;
            }

            response.setContentType(contentType);
            if (printtype != REPORT_PRINTTYPE_HTML)
            {
                String agent = httpServletRequest.getHeader("user-agent");
                String contentDisposition = "attachment; filename=\"" + toUTF8String(reportNameFile+suffix) + "\"";
                if ( agent.contains( "Firefox" ) ) {
                    contentDisposition = "attachment; filename=\"" + MimeUtility.encodeText( reportNameFile + suffix, "UTF8", "B" ) + "\"";
                }

                response.setHeader("Content-Disposition",contentDisposition);
            }
            else {
                String agent = httpServletRequest.getHeader("user-agent");
                String contentDisposition = "filename=\"" + toUTF8String(reportNameFile+suffix) + "\"";
                if ( agent.contains( "Firefox" ) ) {
                    contentDisposition = "filename=\"" + MimeUtility.encodeText( reportNameFile + suffix, "UTF8", "B" ) + "\"";
                }

                response.setHeader("Content-Disposition",contentDisposition);
            }
            OutputStream outputStream = response.getOutputStream();

            switch (printtype) {
                case REPORT_PRINTTYPE_HTML: {

                    JRHtmlExporter jrHtmlExporter = new JRHtmlExporter();
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, jasperPrint);
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.CHARACTER_ENCODING, "UTF-8");
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
                    //  remove empty spaces
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
                    jrHtmlExporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, outputStream);
                    jrHtmlExporter.exportReport();

                    break;
                }

                case REPORT_PRINTTYPE_PDF: {

                    JRPdfExporter exporter = new JRPdfExporter();

                    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

                    exporter.exportReport();

                    break;
                }
                case REPORT_PRINTTYPE_XLS: {

                    JRXlsExporter xlsExporter = new JRXlsExporter();
                    xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    xlsExporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
                    xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                    xlsExporter.exportReport();

                    break;
                }
            }

            outputStream.close();

        } catch (JRException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        } catch (MalformedURLException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        } catch (IOException e) {
            throw new JReportBuildError("Error forming report " + reportName, e);
        }
        return true;
    }

    public JasperReport getReport(String reportName) throws MalformedURLException, JRException {
        JasperReport report;
        if (!compiledReports.containsKey(reportName)) {
            logger.info("Compiling jasper project " + reportName);

            report = JasperCompileManager.compileReport(context.getRealPath("/resources/reports/" + reportName + ".jrxml"));

            // кэширование бинарника отчета
            compiledReports.put(reportName, report);
        } else {
            logger.info("Loading jasper project " + reportName + " from repository");
            report = compiledReports.get(reportName);
        }

        return report;
    }

}
