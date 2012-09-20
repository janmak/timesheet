package com.aplana.timesheet.dao.entity;

import javax.persistence.*;

@Entity
@Table(name = "dictionary", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class Dictionary
{
	@Id
	@Column(nullable = false)
	private Integer id;
	
	@Column(nullable = false)
	private String name;

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder()
			.append(" id=")
			.append(id)
			.append(" name=")
			.append(name)
			.toString();
	}
}