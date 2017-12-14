package cz.expertkom.ju.L08HomeWorkKosikCz.services;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
	//final static String distinctiveForName ="data-page-title=\""; 
	//final static int distinctiveForNameLength = distinctiveForName.length();
	/*doplnit */final static String distinctiveForNameLeft = "                   data-page-title=\"";
	final static int distinctiveForNameLeftLength=distinctiveForNameLeft.length();
	/*doplnit */final static String distinctiveForNameRight = "\"";
	
	/* rozlišovač pro cenu */
	//final static String distinctiveForPrice ="data-product-price=\"";
	//final static int distinctiveForPriceLength = distinctiveForPrice.length();
	/*doplnit */final static String distinctiveForPriceLeft = "                   data-product-price=\"";
	final static int distinctiveForPriceLeftLength=distinctiveForPriceLeft.length();
	/*doplnit */final static String distinctiveForPriceRight = "\"";

	/* NOVÝ !!! - rozlišovač pro cenu po slevě */
	/*doplnit */final static String distinctiveForPriceAfterDiscountLeft = "                <strong class=\"price\">";
	final static int distinctiveForPriceAfterDiscountLeftLength=distinctiveForPriceAfterDiscountLeft.length();
	/*doplnit */final static String distinctiveForPriceAfterDiscountRight = " Kč</strong>";
	
	/* rozlišovač pro Product ID */   
	//final static String distinctiveForIdProduct ="data-product-id=";
	//final int distinctiveForIdProductLength = distinctiveForIdProduct.length();
	/*doplnit */final static String distinctiveForIdProductLeft = "                   data-product-id=\"";
	final static int distinctiveForIdProductLeftLength=distinctiveForIdProductLeft.length();
	/*doplnit */final static String distinctiveForIdProductRight = "\"";

	

	private static final Logger logger =LogManager.getLogger(DownLoadPageServiceImpl.class);
	
	private String stringPage;
	
	private Products prList = new Products(); 
	
	private ProductDto pDto = new ProductDto();
	
	private List<String> listWebPages = new ArrayList<String>();
	
	private String nameFileTxt ="";
	

	private static final String nameFindProducts = "txt/FindProducts.txt";
	private static final File fileFindProducts = new File(nameFindProducts);
	
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
			
		/* --------------------------------------------------------------------- */
		/* stahování wep-pages do txt souboru podle seznamu stahovaných stránek  */
		/* --------------------------------------------------------------------- */
		
		System.out.println("Start načítání....");
		
		for (String uriSection: listWebPages) {
			System.out.println(uriSection);
			try {
				stringPage = Unirest.get(uriSection).asString().getBody();
			} catch (UnirestException e1) {
				logger.error("Problém při čtení ze stránky:"+uriSection,e1);
				System.out.println("Problém při čtení ze stránky:"+uriSection+", "+e1.getLocalizedMessage());
			}
			nameFileTxt = String.format("txt/"+"%03d", indexTxtFile)+"_"+uriSection.replaceAll("https://www.kosik.cz/", "")+".txt";
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
		parseTxtFiles();
	}
	
	
	private void parseTxtFiles() {
		
		/* index textového souboru vzniklého stažením sekce z webové stránky */
		int indexTxtFile =1;
		/* počet zpracovaných produktů - nalezených ve vstupním textovém souboru z webové stránky - včetně duplikovaných produktů */
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
		
		/* zapsání dateTime do souboru nalezených produktů */
		Calendar dateNow = Calendar.getInstance();
		DateFormat sdf = new SimpleDateFormat("d.MMMM.yyyy HH:mm:ss");
		try {
			FileUtils.writeStringToFile(fileFindProducts, "\n\n****************************************\n"+sdf.format(dateNow.getTime())+
															"\n****************************************\n", 
										"UTF-8", true);
		} catch (IOException e1) {
			logger.error("Problém při zápisu do souboru souboru:"+nameFindProducts,e1);
			System.out.println("Problém při čtení ze souboru:"+nameFileTxt+", "+e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		
		/* iterace seznamu webových stránek */
		for (String uriSection: listWebPages) {
			nameFileTxt = String.format("txt/"+"%03d", indexTxtFile)+"_"+uriSection.replaceAll("https://www.kosik.cz/", "")+".txt";
			File inputFile = new File(nameFileTxt);
			try {
				
				boolean nameFound = false;
				boolean priceFound = false;
				boolean priceAfterDiscountFound = false;
				boolean productIdFound = false;
				
				String sname=""; 
				String sprice="";
				String spriceAfterDiscount="";
				
				String sproductid ="";
				
				
				LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
				
				/* iterace textového souboru s obsahem webové stránky  */
				while(it.hasNext()) {
					String rowOfFile = it.nextLine();
					
					/* nalezen Název product */
					if (!nameFound && rowOfFile.contains(distinctiveForNameLeft) && rowOfFile.contains(distinctiveForNameRight) ) {
						nameFound=true;
						//sname = rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForName)+distinctiveForNameLength,rowOfFile.length()-1);
						sname = rowOfFile.substring(rowOfFile.indexOf(distinctiveForNameLeft)+distinctiveForNameLeft.length(),rowOfFile.lastIndexOf(distinctiveForNameRight));
						pDto.setName(sname);
						continue;
					}
					/* nalezena cena produktu */
					if (!priceFound && rowOfFile.contains(distinctiveForPriceLeft) && rowOfFile.contains(distinctiveForPriceRight)) {
						priceFound=true;
						//sprice = rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForPrice)+distinctiveForPriceLength,rowOfFile.length()-1);
						sprice = rowOfFile.substring(rowOfFile.indexOf(distinctiveForPriceLeft)+distinctiveForPriceLeft.length(),rowOfFile.lastIndexOf(distinctiveForPriceRight));
						pDto.setPrice(Float.parseFloat(sprice));
						continue;
					}
					/* nalezena cena produktu po slevě */
					if (!priceAfterDiscountFound && rowOfFile.contains(distinctiveForPriceAfterDiscountLeft) && rowOfFile.contains(distinctiveForPriceAfterDiscountRight)) {
						priceAfterDiscountFound=true;
						//sprice = rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForPrice)+distinctiveForPriceLength,rowOfFile.length()-1);
						//pDto.setPrice(Float.parseFloat(sprice));
						spriceAfterDiscount = rowOfFile.substring(rowOfFile.indexOf(distinctiveForPriceAfterDiscountLeft)+distinctiveForPriceAfterDiscountLeft.length(),rowOfFile.lastIndexOf(distinctiveForPriceAfterDiscountRight));
						spriceAfterDiscount = spriceAfterDiscount.replaceAll(",", ".");
						spriceAfterDiscount=spriceAfterDiscount.replaceAll("\\s", "");
						/* ještě doplnit do Entity, a EntityDto */
						//System.out.println(pDto.getName()+" cena po slevě: "+Float.parseFloat(spriceAfterDiscount));
						pDto.setPriceAfterDiscount(Float.parseFloat(spriceAfterDiscount));
						continue;
					}
					
					/* nalezeno productId produktu - cizí číslo produktu */
					if (! productIdFound && rowOfFile.contains(distinctiveForIdProductLeft) && rowOfFile.contains(distinctiveForIdProductRight)) {
						productIdFound=true;
						//sproductid= rowOfFile.substring(rowOfFile.lastIndexOf(distinctiveForIdProduct)+distinctiveForIdProductLength+1,rowOfFile.length()-1);
						sproductid = rowOfFile.substring(rowOfFile.indexOf(distinctiveForIdProductLeft)+distinctiveForIdProductLeft.length(),rowOfFile.lastIndexOf(distinctiveForIdProductRight));
						pDto.setProductId(sproductid);
					}
					
					/* došlo k nalezení produktu, jeho ceny, ceny po slevě i productID */
					if (nameFound && priceFound && priceAfterDiscountFound && productIdFound) {

						nameFound = false;
						priceFound = false;
						priceAfterDiscountFound = false;
						productIdFound = false;
					
						countProcessedProducts++;
						//System.out.println("nalezen produkt: "+pDto);
						FileUtils.writeStringToFile(fileFindProducts, pDto.toString()+"\n", "UTF-8", true);
						
						/* načtení seznamu produktů z tabulky obsahujících zjištěný název */
						prList = pPrDb.getProductsPerName(pDto.getName());
						
						/* seznam prázdný -> přidat produkt */
						if (prList.getProducts().size() == 0) {
							countInsertedProducts++;
							pPrDb.insertProduct(pDto);
							//System.out.println(pDto.toString());
						}
						
						/* seznam obsahuje právě jeden nalezený produkt -> AKTUALIZOVAT cenu */
						if (prList.getProducts().size() == 1) {
							/* jestliže nebyl touto iterací aktualizován ještě aktualizován */
							if (prList.getProducts().get(0).getIterationStepProcessed()==0) {
								pPrDb.updateProduct(prList.getProducts().get(0).getId(), pDto);
								//System.out.println(pDto.toString());
							} else /* již byl toutu iterací aktualizován */ 
							{
								countDuplicatedInputProducts++;
								FileUtils.writeStringToFile(fileFindProducts, "----------------------------------------------------------------------------------------\n"
																				+ "Zjištěn duplikovaný produkt ve vstupním textovém souboru:"+"\n", "UTF-8", true);
								FileUtils.writeStringToFile(fileFindProducts, "  vstupní soubor  :"+pDto.toString()+"\n", "UTF-8", true);
								FileUtils.writeStringToFile(fileFindProducts, "  tabulka databáze:"+prList.toString()+"\n" , "UTF-8", true);
								FileUtils.writeStringToFile(fileFindProducts, "----------------------------------------------------------------------------------------\n","UTF-8", true);
							}
							
						}
						/* v tabulce nalezeno více řádků se stejným názvem !!!! */
						if (prList.getProducts().size() > 1) {
							countDuplicatedProducts++;
							FileUtils.writeStringToFile(fileFindProducts, "-----------------------------------------------------------------------------------\n", "UTF-8", true);
							FileUtils.writeStringToFile(fileFindProducts, "!!!!! Nalezen duplikovaný produkt v tabulce!!!! : "+pDto.toString()+"\n", "UTF-8", true);
							FileUtils.writeStringToFile(fileFindProducts, "             "+"vstupní textový soubor:" +pDto.toString()+"\n", "UTF-8", true);
							FileUtils.writeStringToFile(fileFindProducts, "             "+"tabulka produktů: "+"\n", "UTF-8", true);
							for (Product pr: prList.getProducts()) {
								FileUtils.writeStringToFile(fileFindProducts, "             "+pr.toString()+"\n", "UTF-8", true);
							}
							FileUtils.writeStringToFile(fileFindProducts, "----------------------------------------------------------------------------------\n", "UTF-8", true);

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

