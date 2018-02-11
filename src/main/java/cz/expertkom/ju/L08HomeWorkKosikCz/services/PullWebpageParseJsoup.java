/*
 * Implementace třídy pro parsování s Jsoup 
 */

package cz.expertkom.ju.L08HomeWorkKosikCz.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class PullWebpageParseJsoup /* implements DownLoadPageService */ {

	// private static final Logger logger =
	// LogManager.getLogger(PullWebpageParseJsoup.class);

	private static List<WebPage> listWebPages = new ArrayList<WebPage>();
	private static List<WebPage> listUriDetailProducts = new ArrayList<WebPage>();
	private static List<WebPage> listWebPagesExtractedDuplicated = new ArrayList<WebPage>();

	private static final String fixedPrefixWebPage = "https://www.kosik.cz";
	private static final String suffixCompleteWebPage = "?do=productList-load";

	/* vytvoření Journalu */
	private static DateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd-HH'hod'-mm'min'-ss'sec'");
//	private static String nameJournal = "txt/Journal " + sdfFile.format(new Date()) + ".txt";
	// private static File journal = new File(nameJournal);

	/* vytvoření Error.logu */
	// private static File errorLog = new File("txt/Error-log.txt");
	//
	// private static File categoriesUriFile = new File("txt/_URI-Categories.txt");
	//
	// private static File productsUriFile = new File("txt/_URI-Products.txt");

	public class WebPage {
		private String urilLink;
		private String fileUriLink;

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

	public class ProductFound {
		String nazev = "";
		float cena = 0;
		String cenaZajednotkuBaleni = "";
		String jednotkaBaleni = "";
		boolean vyprodano = false;
		String velikostBaleni = "";

		public String getNazev() {
			return nazev;
		}

		public void setNazev(String nazev) {
			this.nazev = nazev;
		}

		public float getCena() {
			return cena;
		}

		public void setCena(float cena) {
			this.cena = cena;
		}

		public String getCenaZajednotkuBaleni() {
			return cenaZajednotkuBaleni;
		}

		public void setCenaZajednotkuBaleni(String cenaZajednotkuBaleni) {
			this.cenaZajednotkuBaleni = cenaZajednotkuBaleni;
		}

		public String getJednotkaBaleni() {
			return jednotkaBaleni;
		}

		public void setJednotkaBaleni(String jednotkaBaleni) {
			this.jednotkaBaleni = jednotkaBaleni;
		}

		public boolean isVyprodano() {
			return vyprodano;
		}

		public void setVyprodano(boolean vyprodano) {
			this.vyprodano = vyprodano;
		}

		public String getVelikostBaleni() {
			return velikostBaleni;
		}

		public void setVelikostBaleni(String velikostBaleni) {
			this.velikostBaleni = velikostBaleni;
		}

		@Override
		public String toString() {
			return "ProductFinded [nazev=" + nazev + ", cena=" + cena + ", cenaZajednotkuBaleni=" + cenaZajednotkuBaleni + ", jednotkaBaleni=" + jednotkaBaleni + ", vyprodano=" + vyprodano + ", velikostBaleni=" + velikostBaleni + "]";
		}

	}

	public void start() throws IOException {

		/* přidání "kořenové adresy" do listWebPages */
		listWebPages.add(new WebPage(fixedPrefixWebPage, null));

		/* shození přepínače v databázi */
		// pPrDb.setAllIterationStepsProcessedDown();

		// System.out.println("\n" + nameJournal + "\n\n");

		/* zápis jména žurnálu do error logu */
		// FileUtils.writeStringToFile(errorLog, nameJournal + "\n", "UTF-8", false);
		// FileUtils.writeStringToFile(categoriesUriFile, nameJournal + "\n", "UTF-8",
		// false);
		// FileUtils.writeStringToFile(productsUriFile, nameJournal + "\n", "UTF-8",
		// false);

		/* iterace listWebPages, nově extrahované URI se do něj přidávají */
		int idxListDetailProducts = 0;

		for (int idxListWebPages = 0; idxListWebPages < listWebPages.size(); idxListWebPages++) {

			/* vytvoří název souboru k URI */
			if (listWebPages.get(idxListWebPages).getFileUriLink() == null) {
				listWebPages.get(idxListWebPages).setFileUriLink(createFileNameFormUrilink(listWebPages.get(idxListWebPages).getUrilLink(), "wp", idxListWebPages));
			}
			/* vypíše na konzoli */
			log.info("\n" + idxListWebPages + ":" + listWebPages.get(idxListWebPages).getFileUriLink().substring(4) + ", " + listWebPages.get(idxListWebPages).getUrilLink() + " (URI před zpracováním: " + listWebPages.size()
					+ ", produktů před zpracováním: " + listUriDetailProducts.size() + ")");

			log.info("\nWeb Page:" + listWebPages.get(idxListWebPages).urilLink + ", " + listWebPages.get(idxListWebPages).fileUriLink + "\n" + String.join("", Collections.nCopies(140, "-")) + "-\n");

			/*
			 * stáhne stránku, vytěží URI jiných stránek (přidá do listWebPages) a URI na
			 * detaily produktů, přidá je do listUriDetailProducts) a konec stránku uoží do
			 * html souboru
			 */
			pullWebPageGetUriesSaveToHtml(idxListWebPages);

			while (idxListDetailProducts < listUriDetailProducts.size()) {
				/*
				 * iteruje listUridetailproducts, stahne detail produktu a vytěží požadované
				 * údaje a listDetailProducts nakonec vymaže ?
				 */
				// parseWebPageFromListDetailproducts(idxListDetailProducts);

				idxListDetailProducts++;

			} /* end while */

		} /* endfor */

		// rekapitulace();

	} /* end start */

	private void pullWebPageGetUriesSaveToHtml(int idx) {

		String htmlAddres = listWebPages.get(idx).getUrilLink();

		if (idx != 0 && !htmlAddres.contains("?")) {
			htmlAddres = htmlAddres + suffixCompleteWebPage;
		}

		try {
			Document doc = Jsoup.connect(htmlAddres).get();

			Elements uriDetailProducts = doc.select("a");
			for (Element uriDetailProduct : uriDetailProducts) {
				String uriDetailproduct = uriDetailProduct.absUrl("data-page-path");
				if (!uriDetailproduct.toString().isEmpty() && (!existUriLinkInUriDetailProducts(uriDetailproduct))) {
					listUriDetailProducts.add(new WebPage(uriDetailproduct, null));
					log.info(idx + ": " + uriDetailproduct + "\n");
				}
			}

			/* list selectorů */
			List<String> selectorsUries = new ArrayList<String>();
			selectorsUries.add("div[class=ico-text-group]>a");
			selectorsUries.add("div[class=categories-container]>a");

			for (String selector : selectorsUries) {
				Elements uriOdkazy = doc.select(selector);
				for (Element uriOdkaz : uriOdkazy) {
					String uriWebPage = uriOdkaz.absUrl("href");
					if (!uriWebPage.toString().isEmpty() && (!existUriLinkInListWebPages(uriWebPage))) {
						listWebPages.add(new WebPage(uriWebPage, null));
						log.info(uriWebPage + "\n");

					}
				}
			}

			// File outPutFileName = new File(listWebPages.get(idx).getFileUriLink());
			log.info(doc.toString());
		} catch (IOException e1) {
			log.error(idx + ":" + "     " + "Problém snačtením web.stránky:" + htmlAddres + "\n");
			e1.printStackTrace();
		}

	}

	private void parseWebPageFromListDetailproducts(int idx) {
		String nazev = "";
		float cena = 0;
		String cenaZajednotkuBaleni = "";
		String jednotkaBaleni = "";
		boolean vyprodano = false;
		// System.out.println("M1");
		try {
			Document doc = Jsoup.connect(listUriDetailProducts.get(idx).getUrilLink()).get();
			log.info("A");

			Element nazevZjisteny = doc.select("div [class=block block-title]>h1").first();
			nazev = nazevZjisteny.text().toString();
			log.info("B");

			Element cenaZjistena = doc.select("p[class=product-detail-pricing]>strong[class=price]").first();
			cena = Float.parseFloat(cenaZjistena.text().toString().replaceAll("[Kč' ']", "").replaceAll(",", "."));
			log.info("C");

			Element cenaZajednotkuBaleniZjistena = doc.select("p[class=product-detail-pricing]>small>strong").first();
			cenaZajednotkuBaleni = cenaZajednotkuBaleniZjistena.text().toString().replaceAll("[()]", "");
			log.info("D");

			Element vyprodanoElement = doc.select("p[class=selled-out]").first();
			log.info("E");
			if (vyprodanoElement != null) {
				vyprodano = true;
			}
			log.info("F");
			jednotkaBaleni = cenaZajednotkuBaleni.substring(cenaZajednotkuBaleni.lastIndexOf("/") + 1).trim();
			log.info("G");
			log.info(idx + ": Název:" + nazev + ", Cena:" + Float.toString(cena) + ", Cena za jednotku:" + cenaZajednotkuBaleni + ", Jednotka balení:" + jednotkaBaleni + ((vyprodano) ? ", VYPRODÁNO !!!" : ""));
			log.info("H");
			log.info(idx + ": Název:" + nazev + ", Cena:" + Float.toString(cena) + ", (Cena za jednotku:" + cenaZajednotkuBaleni + "), Jednotka balení:" + jednotkaBaleni + ((vyprodano) ? ", VYPRODÁNO !!!" : ""));
			log.info("I");

		} catch (IOException e) {
			log.error("Problém se stažením detailu produktu: " + listUriDetailProducts.get(idx).getUrilLink() + ", " + listUriDetailProducts.get(idx).getFileUriLink() + "\n");
		}
	}

	private static String createFileNameFormUrilink(String uriString, String prefix, int index) {
		return String.format("txt/" + prefix + "%05d", index + 1) + "_" + uriString.replaceAll("https://www.kosik.cz", "").replaceAll("[/?=]", "_") + ".html";
	}

	public static boolean existUriLinkInListWebPages(String uriLink) {
		boolean uriExists = false;
		for (int i = 0; i < listWebPages.size(); i++) {
			if (listWebPages.get(i).getUrilLink().equals(uriLink)) {
				uriExists = true;
				break;
			}
		}
		return uriExists;
	}

	public static boolean existUriLinkInUriDetailProducts(String uriLink) {
		boolean uriExists = false;
		for (int i = 0; i < listUriDetailProducts.size(); i++) {
			if (listUriDetailProducts.get(i).getUrilLink().equals(uriLink)) {
				uriExists = true;
				break;
			}
		}
		return uriExists;
	}

	private void rekapitulace() {
		System.out.println("\n Rekapitulace");
		log.info("\n Rekapitulace\n");
		log.info("------------\n");
		log.info("\npočet URI nalezených celkem: " + (listWebPages.size() + listWebPagesExtractedDuplicated.size()));
		log.info("       počet unikátních URI: " + listWebPages.size());
		log.info("     počet duplicitních URI: " + listWebPagesExtractedDuplicated.size());
		log.info("\n\nExtracted URI\n----------------------\n");
		for (WebPage wpg : listWebPages) {
			log.info(wpg.toString() + "\n");
		}
	}

}