package kr.ena9ri.newssearch;

import java.text.*;
import java.util.*;

import android.text.*;

public class NewsBean
{
	private String title;
	private String link;
	
	
	public String getTitle()
	{
		return title;
	}
	
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	
	public String getLink()
	{
		return link;
	}
	
	
	public void setLink(String link)
	{
		this.link = link;
	}
	
	
	@Override
	public String toString()
	{
		if (!TextUtils.isEmpty(title))
			return Html.fromHtml(title).toString();
		if (!TextUtils.isEmpty(link))
			return Html.fromHtml(link).toString();
		else
			return "";
	}
}
