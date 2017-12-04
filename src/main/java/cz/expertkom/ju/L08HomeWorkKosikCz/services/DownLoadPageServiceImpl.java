package cz.expertkom.ju.L08HomeWorkKosikCz.services;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.DownLoadPageService;

@Service
public class DownLoadPageServiceImpl implements DownLoadPageService {
	
	private static final Logger logger =LogManager.getLogger(DownLoadPageServiceImpl.class);
	
	private String stringPage;
	
	private List<String> listWebPages = new ArrayList<String>(); 

	
	public void start() {
		this.initListWebPages();
		
		String nameFileTxt;
		System.out.println("Start načítání....");
		for (String uriSection: listWebPages) {
			try {
				stringPage = Unirest.get(uriSection).asString().getBody();
			} catch (UnirestException e1) {
				logger.error("Problém při čtení ze stránky:"+uriSection,e1);
				System.out.println("Problém při čtení ze stránky:"+uriSection+", "+e1.getLocalizedMessage());
			}
			nameFileTxt = uriSection.replaceAll("[:/]", "")+".txt";
			File outPutFileName = new File(nameFileTxt);
			try {
				FileUtils.writeStringToFile(outPutFileName, stringPage, "UTF-8", false);
			} catch (IOException e) {
				logger.error("Problém při zápisu do souboru:"+nameFileTxt,e);
				System.out.println("Problém při zápisu do souboru:"+nameFileTxt+", "+e.getLocalizedMessage());
			}
			System.out.println(uriSection);
		}
		System.out.println("....konec načítání");
		
		
		
		

	}
	
	private void initListWebPages() {
		listWebPages.add("https://www.kosik.cz/mlecne-a-chlazene");
		listWebPages.add("https://www.kosik.cz/maso-a-ryby");
		listWebPages.add("https://www.kosik.cz/ovoce-a-zelenina");
		listWebPages.add("https://www.kosik.cz/pecivo");
		listWebPages.add("https://www.kosik.cz/uzeniny-a-lahudky");
		listWebPages.add("https://www.kosik.cz/mrazene");
		listWebPages.add("https://www.kosik.cz/napoje");
		listWebPages.add("https://www.kosik.cz/trvanlive");
		listWebPages.add("https://www.kosik.cz/mazlicci");
		listWebPages.add("https://www.kosik.cz/deti");
		listWebPages.add("https://www.kosik.cz/domacnost-a-zahrada");
		listWebPages.add("https://www.kosik.cz/drogerie");
		listWebPages.add("https://www.kosik.cz/trafika");
	}

}
