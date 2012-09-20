package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.RegionDAO;
import com.aplana.timesheet.dao.entity.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService
{	
	@Autowired
	private RegionDAO regionDAO;


	public List<Region> getRegions() {	
		return regionDAO.getRegions();
	}
	public Region find(String regionName) {	
		return regionDAO.find(regionName);
	}
	public Region findRegionByCity(String city) {	
		return regionDAO.findRegionByCity(city);
	}
    public Region find(Integer id) {
        return regionDAO.find(id);
    }

}