package cz.expertkom.ju.L08HomeWorkKosikCz.services;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestJsoup {

	/**
	 * *************************************************************************************
	 ** test jak se chová privátní list v beaně do kterého přistupují různé jiné //
	 * beany
	 */
	@Autowired
	SharingListBetweenBeans sharingListBetweenBeans;

	@PostConstruct
	private void addPeople() {
		sharingListBetweenBeans.addPeople("PAVEL", 84.2F);
		sharingListBetweenBeans.addPeople("LUKÁŠ", 25.4F);
	}

	/**
	 * *************************************************************************************
	 **/

	public void jsoupRHR() throws IOException {

		Document doc = Jsoup.connect("https://www.kosik.cz").get();
		System.out.println(doc.title());
		Elements newsHeadlines = doc.select("a");
		for (Element headline : newsHeadlines) {
			System.out.println((headline.attr("title") != null ? "různé od null" : "null") + "|" + headline.absUrl("href") + "|" + headline.absUrl("data-page-path"));
		}

	}

	public void jsoupRHRproduct() throws IOException {

		String nazev = "";
		float cena = 0;
		String cenaZajednotkuBaleni = "";
		String jednotkaBaleni = "";
		boolean vyprodano = false;

		String htmlAdresa = "";
		htmlAdresa = "https://www.kosik.cz/produkt/49893-garnier-moisture-comfort-superhydratacni-zklidnujici-textilni-maska-32g";
		// htmlAdresa="https://www.kosik.cz/produkt/29892-bio-hovezi-zadni-cca-500g";
		htmlAdresa = "https://www.kosik.cz/produkt/19894-coca-cola-sklo-24-x-200ml";
		htmlAdresa = "https://www.kosik.cz/produkt/47689-fixaplast-naplast-classic-textilni-8cm-x-1-m-sleva-dmt-30-12-2020-poskozeny-obal";
		htmlAdresa = "https://www.kosik.cz/produkt/51560-koldokol-malina-sirup-sklo-0-33l";
		htmlAdresa = "https://www.kosik.cz/produkt/3544-bona-vita-jenikuv-lup-cerealni-polstarky-s-cokoladovou-prichuti-250g";

		Document doc = Jsoup.connect(htmlAdresa).get();
		System.out.println("Produkt:" + doc.title());

		Element nazevZjisteny = doc.select("div [class=block block-title]>h1").first();
		nazev = nazevZjisteny.text().toString();

		Element cenaZjistena = doc.select("p[class=product-detail-pricing]>strong[class=price]").first();
		cena = Float.parseFloat(cenaZjistena.text().toString().replaceAll("Kč", "").replaceAll(",", "."));

		Element cenaZajednotkuBaleniZjistena = doc.select("p[class=product-detail-pricing]>small>strong").first();
		cenaZajednotkuBaleni = cenaZajednotkuBaleniZjistena.text().toString().replaceAll("[()]", "");

		Element vyprodanoElement = doc.select("p[class=selled-out]").first();
		if (vyprodanoElement != null) {
			vyprodano = true;
		}

		jednotkaBaleni = cenaZajednotkuBaleni.substring(cenaZajednotkuBaleni.lastIndexOf("/") + 1).trim();

		System.out.println("\nNázev:" + nazev + ", Cena:" + Float.toString(cena) + ", (Cena za jednotku:" + cenaZajednotkuBaleni + "), Jednotka balení:" + jednotkaBaleni + ((vyprodano) ? ", VYPRODÁNO !!!" : "") + "\n");

		File file = new File("txt/_" + htmlAdresa.replace("https://www.kosik.cz/produkt/", "") + ".html");
		FileUtils.writeStringToFile(file, doc.toString(), "UTF-8", false);

	}

	public void jsoupUries() throws IOException {

		String htmlAdresa = "";
		htmlAdresa = "https://www.kosik.cz";

		Document doc = Jsoup.connect(htmlAdresa).get();
		System.out.println("Produkt:" + doc.title());

		int pocetURI = 0;

		Elements uriCategories = doc.select("div[class=ico-text-group]>a");

		for (Element element : uriCategories) {
			pocetURI++;
			System.out.println(pocetURI + ":" + element.absUrl("href"));
		}

		System.out.println("");
		Elements uriCategories2 = doc.select("div[class=categories-container]>a");

		for (Element element : uriCategories2) {
			pocetURI++;
			System.out.println(pocetURI + ":" + element.absUrl("href"));
		}

	}

}
