package com.aplana.timesheet.dao;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JiraDAO {
    private static final Logger logger = LoggerFactory.getLogger(JiraDAO.class);


    public List<Issue> getIssues(JiraRestClient restClient, String jqlQuery) {
        List<Issue> result = new ArrayList<Issue>();
        if (jqlQuery != null && !jqlQuery.equals("")) {
            NullProgressMonitor pm = new NullProgressMonitor();

            SearchRestClient searchClient = restClient.getSearchClient();
            SearchResult searchResult = searchClient.searchJql(jqlQuery, pm);
            Iterable<? extends BasicIssue> issues = searchResult.getIssues();
            for (BasicIssue item :issues) {
                result.add(restClient.getIssueClient().getIssue(item.getKey(), pm));
            }
        }
        return result;
    }

}
