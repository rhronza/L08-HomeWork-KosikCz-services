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

import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.Product;
import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.ProductDto;
import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.Products;
import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.DownLoadPageService;
import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.ProductDbServices;

@Service
public class DownLoadPageServiceImpl implements DownLoadPageService {
	
	@Autowired
	ProductDbServices pPrDb;
	
	/* rozlišovač pro Název */
	final String distinctiveForName ="data-page-title="; 
	int distinctiveForNameLength = distinctiveForName.length();
	
	/* rozlišovač pro cenu */
	String distinctiveForPrice ="data-product-price=";
	int distinctiveForPriceLength = distinctiveForPrice.length();
	
	/* rozlišovač pro Product ID */
	String distinctiveForIdProduct ="data-product-id=";
	int distinctiveForIdProductLength = distinctiveForIdProduct.length();
	

	private static final Logger logger =LogManager.getLogger(DownLoadPageServiceImpl.class);
	
	private String stringPage;
	
	private Products prList = new Products(); 
	
	private ProductDto pDto = new ProductDto();
	
	private List<String> listWebPages = new ArrayList<String>();
	
	private String nameFileTxt ="";
	
	//int i = 0;

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
	}
	
	public void start() {
		
		int indexTxtFile = 1;
			
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
			nameFileTxt = String.format("%03d", indexTxtFile)+"_"+uriSection.replaceAll("https://www.kosik.cz/", "")+".txt";
			File outPutFileName = new File(nameFileTxt);
			try {
				FileUtils.writeStringToFile(outPutFileName, stringPage, "UTF-8", false);
			} catch (IOException e) {
				logger.error("Problém při zápisu do souboru:"+nameFileTxt,e);
				System.out.println("Problém při zápisu do souboru:"+nameFileTxt+", "+e.getLocalizedMessage());
			}
			indexTxtFile++;
		}
	
		System.out.println("....konec načítání");
		
		
		/* ------------------------------ */
		/* převod do databáze			  */
		/* ------------------------------ */
		parseTxtFile();
	}
	
	
	private void parseTxtFile() {
		
		/* index textového souboru vzniklého stažením sekce z webové stránky */
		int indexTxtFile =1;
		/* počet zpracovanýých produktů - nalezených ve vstupním textovém souboru z webové stránky - včetně duplikovaných produktů */
		int countProcessedProducts = 0;
		/* počet nově vložných produktů */
		int countInsertedProducts = 0;
		/* počet duplikovaných produktů v tabulce - narušení konzistence !!*/
		int countDuplicatedProducts = 0;
		/* počet duplikovaných produktů ve vstupním textovém souboru z webové stránky */
		int countDuplicatedInputProducts = 0;
		
		System.out.println("Start převodu do databáze...");
		
		
		/* shození příznaku zpracovaného produktu, nahazují se v insert a update services */ 
		pPrDb.setAllIterationStepsProcessedDown();
		
		/* iterace webových staránek */
		for (String uriSection: listWebPages) {
			nameFileTxt = String.format("%03d", indexTxtFile)+"_"+uriSection.replaceAll("https://www.kosik.cz/", "")+".txt";
			File inputFile = new File(nameFileTxt);
			try {
				
				boolean nameFound = false;
				boolean priceFound = false;
				boolean productIdFound = false;
				
				String sname=""; 
				String sprice="";
				String sproductid ="";
				
				
				LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
				/* iterace textového souboru s obsahem webové stránky  */
				while(it.hasNext()) {
					String rowOfFile = it.nextLine();
					
					/* nalezen Název produktu */
					if (!nameFound && rowOfFile.contains(distinctiveForName)) {
						nameFound=true;
						//sname = rowOfFile.substring(distinctiveForNameLength+1,rowOfFile.length()-1);
						sname = rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForName)+distinctiveForNameLength+1,rowOfFile.length()-1);
						//System.out.println("sname: "+sname);
						pDto.setName(sname);
						continue;
					}
					/* nalezena cena produktu */
					if (!priceFound && rowOfFile.contains(distinctiveForPrice)) {
						priceFound=true;
						sprice = rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForPrice)+distinctiveForPriceLength+1,rowOfFile.length()-1);
						//System.out.println("sprice:"+sprice+".");
						pDto.setPrice(Float.parseFloat(sprice));
						continue;
					}
					/* nalezeno Id produktu */
					if (! productIdFound && rowOfFile.contains(distinctiveForIdProduct)) {
						productIdFound=true;
						sproductid= rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForIdProduct)+distinctiveForIdProductLength+1,rowOfFile.length()-1);
						pDto.setProductId(sproductid);
					}
					
					/* došlo k nalezení produktu, jeho ceny i productID */
					if (nameFound && priceFound && productIdFound) {

						nameFound = false;
						priceFound = false;
						productIdFound = false;
					
						countProcessedProducts++;
						System.out.println("nalezen produkt: "+pDto.getName());
						
						/* načetní seznamu produktů z tabulky obsahujících zjištěný název */
						prList = pPrDb.getProductsPerName(pDto.getName());
						
						/* seznam prázdný -> přidat produkt */
						if (prList.getProducts().size() == 0) {
							countInsertedProducts++;
							pPrDb.insertProduct(pDto);
						}
						
						/* seznam obsahuje právě jeden nalezený produkt -> aktualizovat cenu */
						if (prList.getProducts().size() == 1) {
							/* jestliže nebyl touto iterací aktualizován ještě aktualizován */
							if (prList.getProducts().get(0).getIterationStepProcessed()==0) {
								pPrDb.updateProduct(prList.getProducts().get(0).getId(), pDto);
							} else /* již byl toutu iterací aktualizován */ 
							{
								countDuplicatedInputProducts++;
								System.out.println("Zjištěn duplikovaný produkt ve vstupním souboru:");
								System.out.println("  vstupní soubor  :"+pDto);
								System.out.println("  tabulka databáze:"+prList);
							}
							
						}
						/* v tabulce nalezeno více řádkůl se setejným názvem !!!! */
						if (prList.getProducts().size() > 1) {
							countDuplicatedProducts++;
							System.out.println("---------------------------------------------------------------------------------------------------------");
							System.out.println("!!!!! Nalezen duplikovaný produkt: "+pDto);
							System.out.println("             "+"v tabulce produktů jsou to řádky: ");
							for (Product pr: prList.getProducts()) {
								System.out.println("             "+pr);
							}
							System.out.println("---------------------------------------------------------------------------------------------------------");
						}
					} /* if (nameFound && priceFound && productIdFound) */
				} /* </while>*/
			} catch (IOException e) {
				logger.error("Problém při čtení ze souborudo souboru:"+nameFileTxt,e);
				System.out.println("Problém při čtení ze souboru:"+nameFileTxt+", "+e.getLocalizedMessage());
				e.printStackTrace();
			}
			indexTxtFile++;
		} /* </for> */
		System.out.println("... konec převodu do databáze");
		System.out.println("\n Rekapitulace");
		System.out.println("------------");
		System.out.println("počet stažených prouktů : "+countProcessedProducts);
		System.out.println(" z toho duplikovaných   : "+countDuplicatedInputProducts);
		System.out.println("počet přidaných produktů do tabulky : "+countInsertedProducts);
		System.out.println("počet aktualizovaných produktů      : "+pPrDb.getCountProcessedProduct()); 
		System.out.println("počet neaktualizovaných produktů    : "+pPrDb.getCountNonProcessedProduct()); 
		System.out.println("počet v tabulce duplikovaných produktů (narušená konzistence): "+countDuplicatedProducts);

		
	
	}

}

