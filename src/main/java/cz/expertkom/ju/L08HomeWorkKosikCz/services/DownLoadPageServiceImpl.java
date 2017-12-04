package cz.expertkom.ju.L08HomeWorkKosikCz.services;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.ProductDto;
import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.DownLoadPageService;
import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.ProductDbServices;

@Service
public class DownLoadPageServiceImpl implements DownLoadPageService {
	
	@Autowired
	ProductDbServices pPrDb;

	private static final Logger logger =LogManager.getLogger(DownLoadPageServiceImpl.class);
	
	private String stringPage;
	
	private ProductDto pDto = new ProductDto();
	
	private List<String> listWebPages = new ArrayList<String>();
	
	private String nameFileTxt ="";
	
	//private ResourcePages rPages;
	
	//private List<ResourcePages> rpList = new ArrayList<ResourcePages>();
	
	@PostConstruct 
	public void initListWebPages() {
		
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
		//listWebPages.add("https://www.kosik.cz");
	}
	
	public void start() {
		
		int i = 1;
			
		/* -------------------------------------------------- */
		/* iterace seznamu s stahování stránek do txt souboru */
		/* -------------------------------------------------- */
		
		System.out.println("Start načítání....");
		
		for (String uriSection: listWebPages) {
			System.out.println(uriSection);
			try {
				stringPage = Unirest.get(uriSection).asString().getBody();
			} catch (UnirestException e1) {
				logger.error("Problém při čtení ze stránky:"+uriSection,e1);
				System.out.println("Problém při čtení ze stránky:"+uriSection+", "+e1.getLocalizedMessage());
			}
			nameFileTxt = String.format("%03d", i)+"_"+uriSection.replaceAll("https://www.kosik.cz/", "")+".txt";
			File outPutFileName = new File(nameFileTxt);
			try {
				FileUtils.writeStringToFile(outPutFileName, stringPage, "UTF-8", false);
			} catch (IOException e) {
				logger.error("Problém při zápisu do souboru:"+nameFileTxt,e);
				System.out.println("Problém při zápisu do souboru:"+nameFileTxt+", "+e.getLocalizedMessage());
			}
			
			i++;
		}
		
		System.out.println("....konec načítání");
		
		/* ------------------------------ */
		/* převod do databáze			  */
		/* ------------------------------ */
		parseTxtFile();
	}
	
	
	private void parseTxtFile() {
		/* rozlišovač pro Název */
		String distinctiveForName ="                   data-page-title="; 
		int distinctiveForNameLength = distinctiveForName.length();
		/* rozlišovač pro cenu */
		String distinctiveForPrice ="                  data-product-price=";
		int distinctiveForPriceLength = distinctiveForPrice.length();
		/* rozlišovač pro Product ID */
		String distinctiveForIdProduct ="                   data-product-id=";
		int distinctiveForIdProductLength = distinctiveForIdProduct.length();
		
		int i =1;
		
		System.out.println("Start převodu do databáze...");
		
		for (String uriSection: listWebPages) {
			nameFileTxt = String.format("%03d", i)+"_"+uriSection.replaceAll("https://www.kosik.cz/", "")+".txt";
			File inputFile = new File(nameFileTxt);
			try {
				
				boolean nameFound = false;
				boolean priceFound = false;
				boolean productIdFound = false;
				
				String sname=""; 
				String sprice="";
				String sproductid ="";
				
				
				LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
				/* iterace čtení ze souboru */
				while(it.hasNext()) {
					String rowOfFile = it.nextLine();
					if (rowOfFile.contains(distinctiveForName)) {
						nameFound=true;
						sname = rowOfFile.substring(distinctiveForNameLength+1,rowOfFile.length()-1);
						pDto.setName(sname);
					}
					
					if (rowOfFile.contains(distinctiveForPrice)) {
						priceFound=true;
						sprice = rowOfFile.substring(distinctiveForPriceLength+2,rowOfFile.length()-1);
						pDto.setPrice(Float.parseFloat(sprice));
					}
					
					if (rowOfFile.contains(distinctiveForIdProduct)) {
						productIdFound=true;
						sproductid= rowOfFile.substring(distinctiveForIdProductLength+1,rowOfFile.length()-1);
						pDto.setProductId(sproductid);
					}
					
					if (nameFound && priceFound && productIdFound) {
						nameFound = false;
						priceFound = false;
						productIdFound = false;
						/* logika:
						 * je produkt s daným názvem obsažen ? (jak se má chovat pokud najdeme více produktů 
						 * ANO -> aktualizuj cenu a datum updated
						 * NE -> přidej produkt
						 */
						
						/*pPrDb.insertProduct(pDto);*/
					}
				}
				
			} catch (IOException e) {
				logger.error("Problém při čtení ze souborudo souboru:"+nameFileTxt,e);
				System.out.println("Problém při čtení ze souboru:"+nameFileTxt+", "+e.getLocalizedMessage());
				e.printStackTrace();
			}
			i++;
		} /* end for */
		System.out.println("... konec převodu do databáze");
	
	}

}
