package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "dictionary_item", uniqueConstraints = @UniqueConstraint(columnNames = {"value", "dict_id"}))
public class DictionaryItem
{
	@Id
	@Column(nullable = false)
	private Integer id;
	
	@Column(nullable = false)
	private String value;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dict_id", nullable = false)
	@ForeignKey(name = "FK_DICTIONARY")
	private Dictionary dictionary;

	public Dictionary getDictionary()
	{
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary)
	{
		this.dictionary = dictionary;
	}

	public Integer getId()
	{
		return id;
	}

	public String getValue()
	{
		return value;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder()		
			.append(" id=")
			.append(id)
			.append(" value=")
			.append(value)
			.append(" dictionary [")
			.append(dictionary)
			.append("]")
			.toString();
	}
}