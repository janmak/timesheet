package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryDAO;
import com.aplana.timesheet.dao.entity.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryService {
    @Autowired
    DictionaryDAO dictionaryDAO;

    public Dictionary find( Integer id ) {
        return dictionaryDAO.find( id );
    }

    public List<Dictionary> getDictionaries() {
        return dictionaryDAO.getDictionaries();
    }
}