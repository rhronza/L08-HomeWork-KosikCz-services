package cz.expertkom.ju.L08HomeWorkKosikCz.services;

/** 
 * verze parsování web stránky skrze string funkce - původní přístup 
 * 
 * **/

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	final static String distinctiveForNameLeft = "                   data-page-title=\"";
	final static int distinctiveForNameLeftLength = distinctiveForNameLeft.length();
	/* doplnit */final static String distinctiveForNameRight = "\"";

	/* rozlišovač pro cenu */
	final static String distinctiveForPriceLeft = "                   data-product-price=\"";
	final static int distinctiveForPriceLeftLength = distinctiveForPriceLeft.length();
	final static String distinctiveForPriceRight = "\"";

	/* NOVÝ !!! - rozlišovač pro cenu po slevě */
	final static String distinctiveForPriceAfterDiscountLeft = "                <strong class=\"price\">";
	final static int distinctiveForPriceAfterDiscountLeftLength = distinctiveForPriceAfterDiscountLeft.length();
	final static String distinctiveForPriceAfterDiscountRight = " Kč</strong>";

	/* rozlišovač pro Product ID */
	final static String distinctiveForIdProductLeft = "                   data-product-id=\"";
	final static int distinctiveForIdProductLeftLength = distinctiveForIdProductLeft.length();
	final static String distinctiveForIdProductRight = "\"";

	/* rozlišovač pro URI */
	final static String dictinctiveURILeft = "<a href=\"";
	final static int dictinctiveURILeftLength = dictinctiveURILeft.length();
	final static String dictinctiveURIRight = "\"";
	// final static String firsrCharUri = "/";

	private static final Logger logger = LogManager.getLogger(DownLoadPageServiceImpl.class);

	private static String stringPage;

	private static Products prList = new Products();

	private static ProductDto pDto = new ProductDto();

	private static List<WebPage> listWebPages = new ArrayList<WebPage>();
	// private static List<WebPage> listWebPagesExtracted = new
	// ArrayList<WebPage>();
	private static List<WebPage> listWebPagesExtractedDuplicated = new ArrayList<WebPage>();

	private static final String fixedPrefixWebPage = "https://www.kosik.cz";

	private static final String correctSequenceDeatil = "1234";

	private static int countProcessedProducts = 0;
	private static int countInsertedProducts = 0;
	private static int countDuplicatedInputProducts = 0;
	private static int countDuplicatedProducts = 0; // v tabulce - narušená konzistance

	/*
	 * private List <String> listUriFixedPrefix = new ArrayList<String>(); private
	 * static final String distincticeUriFixedPrefixLeft=""; private static final
	 * String distincticeUriFixedPrefixRight="";
	 */

	/* index textového souboru vzniklého stažením sekce z webové stránky */
	// int indexTxtFile =1;

	/*
	 * proměnná do které se ukládá název textového souboru pro iterování textového
	 * souboru
	 */
	// private String nameFileTxt ="";

	/* vytvoření jména Journalu */
	private static DateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd-HH'hod'-mm'min'-ss'sec'");
	private static String nameJournal = "txt/Journal " + sdfFile.format(new Date()) + ".txt";
	private static File journal = new File(nameJournal);

	// int i = 0;

	public class WebPage {
		private String urilLink;
		private String fileUriLink;
		// private boolean processed;

		public WebPage(String urilLink, String fileUriLink) {
			this.urilLink = urilLink;
			this.fileUriLink = fileUriLink;
		}

		public String getUrilLink() {
			return urilLink;
		}

		public void setUrilLink(String urilLink) {
			this.urilLink = urilLink;
		}

		public String getFileUriLink() {
			return fileUriLink;
		}

		public void setFileUriLink(String fileUriLink) {
			this.fileUriLink = fileUriLink;
		}

		@Override
		public String toString() {
			return "WebPage [urilLink=" + urilLink + ", fileUriLink=" + fileUriLink + "]";
		}
	}

	@PostConstruct
	public void initListWebPages() {
		// listWebPages.add(new
		// WebPage("https://www.kupi.cz/sleva/pivo-svetle-vycepni-original-10-gambrinus","txt/0001_kupicz.txt"));

		listWebPages.add(new WebPage("https://www.kosik.cz", null));
		/*
		 * listWebPages.add(new WebPage("https://www.kosik.cz/mlecne-a-chlazene",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/maso-a-ryby",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/ovoce-a-zelenina",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/pecivo",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/uzeniny-a-lahudky",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/mrazene",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/napoje",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/trvanlive",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/mazlicci",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/deti",null));
		 * listWebPages.add(new
		 * WebPage("https://www.kosik.cz/domacnost-a-zahrada",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/drogerie",null));
		 * listWebPages.add(new WebPage("https://www.kosik.cz/trafika",null));
		 */

	}

	public void start() {

		/* shození přepínače v databázi */
		pPrDb.setAllIterationStepsProcessedDown();

		System.out.println();
		System.out.println(nameJournal);
		System.out.println("");
		System.out.println();

		/* iterace listWebPages, nově extrahované se do něj přidávají */
		for (int idx = 0; idx < listWebPages.size(); idx++) {
			if (listWebPages.get(idx).getFileUriLink() == null) {
				listWebPages.get(idx).setFileUriLink(createFileNameFormUrilink(idx));
			}
			System.out.println(listWebPages.get(idx).getFileUriLink().substring(4) + " (URI před zpracováním: " + listWebPages.size() + ")");

			/* zápis aktuálního Uri link do Journalu */
			try {
				FileUtils.writeStringToFile(journal, "\n" + listWebPages.get(idx).fileUriLink + ", " + listWebPages.get(idx).urilLink + "\n" + String.join("", Collections.nCopies(140, "-")) + "-\n", "UTF-8", true);
			} catch (IOException e9) {
				logger.error("(100)Problém při zápisu do souboru:" + nameJournal, e9);
				System.out.println("(100)Problém při zápisu do souboru:" + nameJournal + ", " + e9.getLocalizedMessage());
				e9.printStackTrace();
			}

			/* stažení stránky */
			downLoadPageSaveToTxtFile(idx);

			/* parsuj stránky: extrakce urilink a parametrů vyrobků */
			parseTxtFile(idx);

			/*
			 * if (((idx +1) % 20 )== 0) { break; }
			 */
		}

		try {
			System.out.println("\n Rekapitulace");
			FileUtils.writeStringToFile(journal, "\n Rekapitulace\n", "UTF-8", true);

			System.out.println("------------");
			FileUtils.writeStringToFile(journal, "------------\n", "UTF-8", true);

			System.out.println("počet stažených produktů : " + countProcessedProducts);
			FileUtils.writeStringToFile(journal, "počet stažených produktů : " + countProcessedProducts + "\n", "UTF-8", true);

			System.out.println("    z toho duplikovaných : " + countDuplicatedInputProducts);
			FileUtils.writeStringToFile(journal, "    z toho duplikovaných : " + countDuplicatedInputProducts + "\n", "UTF-8", true);

			System.out.println("počet přidaných produktů do tabulky : " + countInsertedProducts);
			FileUtils.writeStringToFile(journal, "počet přidaných produktů do tabulky : " + countInsertedProducts + "\n", "UTF-8", true);

			System.out.println("\nPočet produktů v tabulce celkem : " + pPrDb.countProducts());
			FileUtils.writeStringToFile(journal, "\nPočet produktů v tabulce celkem : " + pPrDb.countProducts() + "\n", "UTF-8", true);

			System.out.println("   zpracovaných (přidaných a aktualizovaných): " + pPrDb.getCountProcessedProduct());
			FileUtils.writeStringToFile(journal, "   zpracovaných (přidaných a aktualizovaných): " + pPrDb.getCountProcessedProduct() + "\n", "UTF-8", true);

			System.out.println("                               nezpracovaných: " + pPrDb.getCountNonProcessedProduct());
			FileUtils.writeStringToFile(journal, "                               nezpracovaných: " + pPrDb.getCountNonProcessedProduct() + "\n", "UTF-8", true);

			System.out.println("\npočet v tabulce duplikovaných produktů (narušená konzistence): " + countDuplicatedProducts);
			FileUtils.writeStringToFile(journal, "\npočet v tabulce duplikovaných produktů (narušená konzistence): " + countDuplicatedProducts + "\n", "UTF-8", true);

			/*
			 * System.out.println("počet URI basic: "+listWebPages.size());
			 * FileUtils.writeStringToFile(journal,
			 * "počet URI basic: "+listWebPages.size()+"\n", "UTF-8", true);
			 */

			System.out.println("\npočet URI nalezených celkem: " + (listWebPages.size() + listWebPagesExtractedDuplicated.size()));
			FileUtils.writeStringToFile(journal, "\npočet URI nalezených celkem: " + (listWebPages.size() + listWebPagesExtractedDuplicated.size()) + "\n", "UTF-8", true);

			System.out.println("       počet unikátních URI: " + listWebPages.size());
			FileUtils.writeStringToFile(journal, "       počet unikátních URI: " + listWebPages.size() + "\n", "UTF-8", true);

			System.out.println("     počet duplicitních URI: " + listWebPagesExtractedDuplicated.size());
			FileUtils.writeStringToFile(journal, "     počet duplicitních URI: " + listWebPagesExtractedDuplicated.size() + "\n", "UTF-8", true);

			FileUtils.writeStringToFile(journal, "\n\nExtracted URI\n----------------------\n", "UTF-8", true);
			for (WebPage wpg : listWebPages) {
				FileUtils.writeStringToFile(journal, wpg.toString() + "\n", "UTF-8", true);
			}

			/*
			 * FileUtils.writeStringToFile(journal,
			 * "\n\nExtracted URI duplicated\n---------------------------------------\n",
			 * "UTF-8", true); for (WebPage wpgd : listWebPagesExtractedDuplicated) {
			 * FileUtils.writeStringToFile(journal, wpgd.toString()+"\n", "UTF-8", true); }
			 */

		} catch (IOException e) {
			logger.error("(101)Problém při zápisu do souboru:" + nameJournal, e);
			System.out.println("(101)Problém při zápisu do souboru:" + nameJournal + ", " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private static void downLoadPageSaveToTxtFile(int idx) {
		try {
			stringPage = Unirest.get(listWebPages.get(idx).getUrilLink()).asString().getBody();
		} catch (UnirestException e1) {
			logger.error("(102)Problém při čtení ze stránky:" + listWebPages.get(idx).getUrilLink(), e1);
			System.out.println("(102)Problém při čtení ze stránky:" + listWebPages.get(idx).getUrilLink() + ", " + e1.getLocalizedMessage());
		}
		File outPutFileName = new File(listWebPages.get(idx).getFileUriLink());
		try {
			FileUtils.writeStringToFile(outPutFileName, stringPage, "UTF-8", false);
		} catch (IOException e) {
			logger.error("(103)Problém při zápisu do souboru:" + listWebPages.get(idx).getFileUriLink(), e);
			System.out.println("(103)Problém při zápisu do souboru:" + listWebPages.get(idx).getFileUriLink() + ", " + e.getLocalizedMessage());
		}
	}

	private void parseTxtFile(int idx) {
		File inputFile = new File(listWebPages.get(idx).fileUriLink);

		String sequenceDetail = "";

		boolean nameFound = false;
		boolean priceFound = false;
		boolean priceAfterDiscountFound = false;
		boolean productIdFound = false;

		try {

			LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");

			String sname = "";
			String sprice = "";
			String spriceAfterDiscount = "";
			String sUri = "";
			String firstCharAfterLeftDistinctive = "";
			String sproductid = "";

			/* iterace textového souboru s obsahem webové stránky */
			while (it.hasNext()) {

				/* načtení řádku textového souboru */
				String rowOfFile = it.nextLine();

				int leftPosition = -1;
				int rightPosition = -1;

				/* hledání URI */
				leftPosition = rowOfFile.indexOf(dictinctiveURILeft);
				if (leftPosition > -1) {
					/* znak následující za dictinctiveURILeft */
					firstCharAfterLeftDistinctive = rowOfFile.substring(leftPosition + dictinctiveURILeftLength, leftPosition + dictinctiveURILeftLength + 1);
					/* jestliže začíná na"/" */
					if ("/".equals(firstCharAfterLeftDistinctive)) {
						sUri = rowOfFile.substring(leftPosition + dictinctiveURILeftLength);
						rightPosition = sUri.indexOf(dictinctiveURIRight);
						sUri = sUri.substring(0, rightPosition);
						/* přidání do listu */
						if (!existUriLinkInListWebPages(fixedPrefixWebPage + sUri)) {
							listWebPages.add(new WebPage(fixedPrefixWebPage + sUri, null));
						} else {
							listWebPagesExtractedDuplicated.add(new WebPage(fixedPrefixWebPage + sUri, null));
						}
					}
				}

				/* hledání Názvu product */
				leftPosition = rowOfFile.indexOf(distinctiveForNameLeft);
				rightPosition = rowOfFile.indexOf(distinctiveForNameRight);
				if (!nameFound && leftPosition > -1 && leftPosition < rightPosition) {
					nameFound = true;
					sequenceDetail = sequenceDetail + "3";
					sname = rowOfFile.substring(leftPosition + distinctiveForNameLeftLength);
					rightPosition = sname.indexOf(distinctiveForNameRight);
					sname = sname.substring(0, rightPosition);
					pDto.setName(sname);
					continue;
				}

				/* nalezena cena produktu */
				if (!priceFound && rowOfFile.contains(distinctiveForPriceLeft) && rowOfFile.contains(distinctiveForPriceRight)) {
					priceFound = true;
					sequenceDetail = sequenceDetail + "2";
					sprice = rowOfFile.substring(rowOfFile.indexOf(distinctiveForPriceLeft) + distinctiveForPriceLeft.length(), rowOfFile.lastIndexOf(distinctiveForPriceRight));
					pDto.setPrice(Float.parseFloat(sprice));
					continue;
				}
				/* nalezena cena produktu po slevě */
				if (!priceAfterDiscountFound && rowOfFile.contains(distinctiveForPriceAfterDiscountLeft) && rowOfFile.contains(distinctiveForPriceAfterDiscountRight)) {
					priceAfterDiscountFound = true;
					sequenceDetail = sequenceDetail + "4";
					spriceAfterDiscount = rowOfFile.substring(rowOfFile.indexOf(distinctiveForPriceAfterDiscountLeft) + distinctiveForPriceAfterDiscountLeft.length(), rowOfFile.lastIndexOf(distinctiveForPriceAfterDiscountRight));
					spriceAfterDiscount = spriceAfterDiscount.replaceAll(",", ".");
					spriceAfterDiscount = spriceAfterDiscount.replaceAll("\\s", "");
					/* ještě doplnit do Entity, a EntityDto */
					pDto.setPriceAfterDiscount(Float.parseFloat(spriceAfterDiscount));
					continue;
				}

				/* nalezeno productId produktu - cizí číslo produktu */
				if (!productIdFound && rowOfFile.contains(distinctiveForIdProductLeft) && rowOfFile.contains(distinctiveForIdProductRight)) {
					productIdFound = true;
					sequenceDetail = sequenceDetail + "1";
					sproductid = rowOfFile.substring(rowOfFile.indexOf(distinctiveForIdProductLeft) + distinctiveForIdProductLeft.length(), rowOfFile.lastIndexOf(distinctiveForIdProductRight));
					pDto.setProductId(sproductid);
				}

				/*
				 * došlo k nalezení produktu, jeho ceny, ceny po slevě i productID --> NÁSLEDUJE
				 * POROVNÁNÍ S TABULKOU A PŘÍPADNÝ ZÁPIS
				 */
				if (nameFound && priceFound && priceAfterDiscountFound && productIdFound) {

					nameFound = false;
					priceFound = false;
					priceAfterDiscountFound = false;
					productIdFound = false;

					/* inkrementace počtu extrahovaných produktů */
					countProcessedProducts++;

					/* zapis nalezeného produktu do Journalu */
					if (!correctSequenceDeatil.equals(sequenceDetail)) {
						FileUtils.writeStringToFile(journal, "NESHODUJE SE sequence detail!!! \n", "UTF-8", true);
					}
					FileUtils.writeStringToFile(journal, sequenceDetail + " " + pDto.toString() + "\n", "UTF-8", true);

					/* načtení seznamu produktů z tabulky obsahujících zjištěný název */
					prList = pPrDb.getProductsPerName(pDto.getName());

					/* seznam prázdný -> přidat produkt */
					if (prList.getProducts().size() == 0) {
						countInsertedProducts++;
						pPrDb.insertProduct(pDto);
					}

					/* seznam obsahuje právě jeden nalezený produkt -> AKTUALIZOVAT cenu */
					if (prList.getProducts().size() == 1) {
						/* jestliže nebyl touto iterací dosud aktualizován: */
						if (prList.getProducts().get(0).getIterationStepProcessed() == 0) {
							pPrDb.updateProduct(prList.getProducts().get(0).getId(), pDto);
						} else /* již byl toutu iterací aktualizován: */
						{
							countDuplicatedInputProducts++;
							FileUtils.writeStringToFile(journal, "----------------------------------------------------------------------------------------\n" + "Zjištěn duplikovaný produkt ve vstupním textovém souboru:" + "\n", "UTF-8",
									true);
							FileUtils.writeStringToFile(journal, (pDto.getProductId().equals(prList.getProducts().get(0).getProductId())) ? "ProductID se shoduje" : "ProductID se NESHODUJE !!!!", "UTF-8", true);
							FileUtils.writeStringToFile(journal, ((pDto.getPriceAfterDiscount() == prList.getProducts().get(0).getPriceAfterDiscount()) ? ", Cena se shoduje" : ", Cena se NESHODUJE !!!!") + "\n", "UTF-8", true);
							FileUtils.writeStringToFile(journal, "  vstupní soubor  : " + sequenceDetail + " " + pDto.toString() + "\n", "UTF-8", true);
							FileUtils.writeStringToFile(journal, "  tabulka databáze:" + prList.toString() + "\n", "UTF-8", true);
							FileUtils.writeStringToFile(journal, "----------------------------------------------------------------------------------------\n", "UTF-8", true);
						}
					}
					/* v tabulce nalezeno více řádků se stejným názvem !!!!: */
					if (prList.getProducts().size() > 1) {
						countDuplicatedProducts++;
						FileUtils.writeStringToFile(journal, "-----------------------------------------------------------------------------------\n", "UTF-8", true);
						FileUtils.writeStringToFile(journal, "!!!!! Nalezen duplikovaný produkt v tabulce!!!! : " + pDto.toString() + "\n", "UTF-8", true);
						FileUtils.writeStringToFile(journal, "             " + "vstupní textový soubor: " + sequenceDetail + " " + pDto.toString() + "\n", "UTF-8", true);
						FileUtils.writeStringToFile(journal, "             " + "tabulka produktů: " + "\n", "UTF-8", true);
						for (Product pr : prList.getProducts()) {
							FileUtils.writeStringToFile(journal, "             " + pr.toString() + "\n", "UTF-8", true);
						}
						FileUtils.writeStringToFile(journal, "----------------------------------------------------------------------------------\n", "UTF-8", true);

					}
					sequenceDetail = "";

				} /* <if/ (nameFound && priceFound && productIdFound) */

			} /* </while> */
			it.close();

		} catch (IOException e) {
			logger.error("(105)Problém při zápisu do souboru:" + nameJournal, e);
			System.out.println("(105)Problém při zápisu do souboru:" + nameJournal + ", " + e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	private static String createFileNameFormUrilink(int idx) {
		String uriLink = listWebPages.get(idx).getUrilLink();
		return String.format("txt/%05d", idx + 1) + "_" + uriLink.replaceAll("https://www.kosik.cz", "").replaceAll("[/?=]", "_") + ".txt";
	}

	public static boolean existUriLinkInListWebPages(String uriLink) {
		boolean returnCode = false;
		for (int i = 0; i < listWebPages.size(); i++) {
			if (listWebPages.get(i).getUrilLink().equals(uriLink)) {
				returnCode = true;
				break;
			}
		}
		return returnCode;
	}
}