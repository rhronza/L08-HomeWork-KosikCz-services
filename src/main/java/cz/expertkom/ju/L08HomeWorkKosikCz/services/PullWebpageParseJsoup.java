/*
 * Implementace třídy pro parsování s Jsoup 
 */

package cz.expertkom.ju.L08HomeWorkKosikCz.services;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class PullWebpageParseJsoup /* implements DownLoadPageService */ {

	private static final Logger logger = LogManager.getLogger(PullWebpageParseJsoup.class);

	private static List<WebPage> listWebPages = new ArrayList<WebPage>();
	private static List<WebPage> listUriDetailProducts = new ArrayList<WebPage>();
	private static List<WebPage> listWebPagesExtractedDuplicated = new ArrayList<WebPage>();

	private static final String fixedPrefixWebPage = "https://www.kosik.cz";
	private static final String suffixCompleteWebPage = "?do=productList-load";

	/* vytvoření Journalu */
	private static DateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd-HH'hod'-mm'min'-ss'sec'");
	private static String nameJournal = "txt/Journal " + sdfFile.format(new Date()) + ".txt";
	private static File journal = new File(nameJournal);

	/* vytvoření Error.logu */
	private static File errorLog = new File("txt/Error-log.txt");

	private static File categoriesUriFile = new File("txt/_URI-Categories.txt");

	private static File productsUriFile = new File("txt/_URI-Products.txt");

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
			return "ProductFinded [nazev=" + nazev + ", cena=" + cena + ", cenaZajednotkuBaleni=" + cenaZajednotkuBaleni
					+ ", jednotkaBaleni=" + jednotkaBaleni + ", vyprodano=" + vyprodano + ", velikostBaleni="
					+ velikostBaleni + "]";
		}

	}

	public void start() throws IOException {

		/* přidání "kořenové adresy" do listWebPages */
		listWebPages.add(new WebPage(fixedPrefixWebPage, null));

		/* shození přepínače v databázi */
		// pPrDb.setAllIterationStepsProcessedDown();

		System.out.println("\n" + nameJournal + "\n\n");

		/* zápis jména žurnálu do error logu */
		FileUtils.writeStringToFile(errorLog, nameJournal + "\n", "UTF-8", false);
		FileUtils.writeStringToFile(categoriesUriFile, nameJournal + "\n", "UTF-8", false);
		FileUtils.writeStringToFile(productsUriFile, nameJournal + "\n", "UTF-8", false);

		/* iterace listWebPages, nově extrahované URI se do něj přidávají */
		int idxListDetailProducts = 0;

		for (int idxListWebPages = 0; idxListWebPages < listWebPages.size(); idxListWebPages++) {

			/* vytvoří název souboru k URI */
			if (listWebPages.get(idxListWebPages).getFileUriLink() == null) {
				listWebPages.get(idxListWebPages).setFileUriLink(createFileNameFormUrilink(
						listWebPages.get(idxListWebPages).getUrilLink(), "wp", idxListWebPages));
			}
			/* vypíše na konzoli */
			System.out.println("\n" + idxListWebPages + ":"
					+ listWebPages.get(idxListWebPages).getFileUriLink().substring(4) + ", "
					+ listWebPages.get(idxListWebPages).getUrilLink() + " (URI před zpracováním: " + listWebPages.size()
					+ ", produktů před zpracováním: " + listUriDetailProducts.size() + ")");

			/* zapíše aktuální Uri link a jméno souboru do Journalu */
			FileUtils.writeStringToFile(journal,
					"\nWeb Page:" + listWebPages.get(idxListWebPages).urilLink + ", "
							+ listWebPages.get(idxListWebPages).fileUriLink + "\n"
							+ String.join("", Collections.nCopies(140, "-")) + "-\n",
					"UTF-8", true);

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
				//parseWebPageFromListDetailproducts(idxListDetailProducts);
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
					FileUtils.writeStringToFile(productsUriFile, idx + ": " + uriDetailproduct + "\n", "UTF-8", true);
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
						FileUtils.writeStringToFile(categoriesUriFile, uriWebPage + "\n", "UTF-8", true);

					}
				}
			}

			File outPutFileName = new File(listWebPages.get(idx).getFileUriLink());
			try {
				FileUtils.writeStringToFile(outPutFileName, doc.toString(), "UTF-8", false);
			} catch (IOException e) {
				logger.error("(103)Problém při zápisu do souboru:" + listWebPages.get(idx).getFileUriLink(), e);
				System.out.println("(103)Problém při zápisu do souboru:" + listWebPages.get(idx).getFileUriLink() + ", "
						+ e.getLocalizedMessage());
			}
		} catch (IOException e1) {
			try {
				FileUtils.writeStringToFile(errorLog,
						idx + ":" + "     " + "Problém snačtením web.stránky:" + htmlAddres + "\n", "UTF-8", true);
			} catch (IOException e111) {
				System.out.println("(222)Problém při zápisu do chybového logu:" + e111.getLocalizedMessage());
			}
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
			// System.out.println("M2");
			// System.out.println("Produkt:"+doc.title());
			FileUtils.writeStringToFile(journal, "A", "UTF-8", true);

			Element nazevZjisteny = doc.select("div [class=block block-title]>h1").first();
			nazev = nazevZjisteny.text().toString();
			FileUtils.writeStringToFile(journal, "B", "UTF-8", true);

			Element cenaZjistena = doc.select("p[class=product-detail-pricing]>strong[class=price]").first();
			cena = Float.parseFloat(cenaZjistena.text().toString().replaceAll("[Kč' ']", "").replaceAll(",", "."));
			FileUtils.writeStringToFile(journal, "C", "UTF-8", true);

			Element cenaZajednotkuBaleniZjistena = doc.select("p[class=product-detail-pricing]>small>strong").first();
			cenaZajednotkuBaleni = cenaZajednotkuBaleniZjistena.text().toString().replaceAll("[()]", "");
			FileUtils.writeStringToFile(journal, "D", "UTF-8", true);

			Element vyprodanoElement = doc.select("p[class=selled-out]").first();
			FileUtils.writeStringToFile(journal, "E", "UTF-8", true);
			if (vyprodanoElement != null) {
				vyprodano = true;
			}
			FileUtils.writeStringToFile(journal, "F", "UTF-8", true);
			jednotkaBaleni = cenaZajednotkuBaleni.substring(cenaZajednotkuBaleni.lastIndexOf("/") + 1).trim();
			FileUtils.writeStringToFile(journal, "G", "UTF-8", true);
			System.out.println("     " + idx + ": Název:" + nazev + ", Cena:" + Float.toString(cena)
					+ ", Cena za jednotku:" + cenaZajednotkuBaleni + ", Jednotka balení:" + jednotkaBaleni
					+ ((vyprodano) ? ", VYPRODÁNO !!!" : ""));
			FileUtils.writeStringToFile(journal, "H", "UTF-8", true);
			FileUtils.writeStringToFile(journal,
					"     " + idx + ": Název:" + nazev + ", Cena:" + Float.toString(cena) + ", (Cena za jednotku:"
							+ cenaZajednotkuBaleni + "), Jednotka balení:" + jednotkaBaleni
							+ ((vyprodano) ? ", VYPRODÁNO !!!" : "") + "\n",
					"UTF-8", true);
			FileUtils.writeStringToFile(journal, "I", "UTF-8", true);

		} catch (IOException e) {
			try {
				FileUtils.writeStringToFile(errorLog,
						"     " + "     " + "Problém se stažením detailu produktu: "
								+ listUriDetailProducts.get(idx).getUrilLink() + ", "
								+ listUriDetailProducts.get(idx).getFileUriLink() + "\n",
						"UTF-8", true);
			} catch (IOException e112) {
				System.out.println("(223)Problém při zápisu do chybového logu:" + e112.getLocalizedMessage());
			}
		}

	}

	private static String createFileNameFormUrilink(String uriString, String prefix, int index) {
		return String.format("txt/" + prefix + "%05d", index + 1) + "_"
				+ uriString.replaceAll("https://www.kosik.cz", "").replaceAll("[/?=]", "_") + ".html";
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
		try {
			System.out.println("\n Rekapitulace");
			FileUtils.writeStringToFile(journal, "\n Rekapitulace\n", "UTF-8", true);

			System.out.println("------------");
			FileUtils.writeStringToFile(journal, "------------\n", "UTF-8", true);

			/*
			 * System.out.println("počet URI basic: "+listWebPages.size());
			 * FileUtils.writeStringToFile(journal,
			 * "počet URI basic: "+listWebPages.size()+"\n", "UTF-8", true);
			 */

			System.out.println(
					"\npočet URI nalezených celkem: " + (listWebPages.size() + listWebPagesExtractedDuplicated.size()));
			FileUtils.writeStringToFile(journal, "\npočet URI nalezených celkem: "
					+ (listWebPages.size() + listWebPagesExtractedDuplicated.size()) + "\n", "UTF-8", true);

			System.out.println("       počet unikátních URI: " + listWebPages.size());
			FileUtils.writeStringToFile(journal, "       počet unikátních URI: " + listWebPages.size() + "\n", "UTF-8",
					true);

			System.out.println("     počet duplicitních URI: " + listWebPagesExtractedDuplicated.size());
			FileUtils.writeStringToFile(journal,
					"     počet duplicitních URI: " + listWebPagesExtractedDuplicated.size() + "\n", "UTF-8", true);

			FileUtils.writeStringToFile(journal, "\n\nExtracted URI\n----------------------\n", "UTF-8", true);
			for (WebPage wpg : listWebPages) {
				FileUtils.writeStringToFile(journal, wpg.toString() + "\n", "UTF-8", true);
			}

		} catch (IOException e) {
			logger.error("(101)Problém při zápisu do souboru:" + nameJournal, e);
			System.out.println("(101)Problém při zápisu do souboru:" + nameJournal + ", " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

}