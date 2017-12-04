package cz.expertkom.ju.L08HomeWorkKosikCz.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.Product;
import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.ProductDto;
import cz.expertkom.ju.L08HomeWorkKosikCz.Entity.Products;
import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.ProductDbServices;
import cz.expertkom.ju.L08HomeWorkKosikCz.interfaces.ProductRepository;

@Service
public class ProductDbServicesImpl implements ProductDbServices {
	
	@Autowired
	ProductRepository pRep;

	public Products getOne(Long id) {
		Products p = new Products();
		List<Product> pl = new ArrayList<Product>();
		pl.add(pRep.findOne(id));
		p.setProducts(pl);
		return p;
	}
	
	public ProductDto getOne2(Long id) {
		Product p = pRep.findOne(id);
		ProductDto pDto = new ProductDto();
		pDto.setId(p.getId());
		pDto.setName(p.getName());
		pDto.setPrice(p.getPrice());
		pDto.setInsertedTimeStamp(p.getInsertedTimeStamp());
		pDto.setUpdatedTimeStamp(p.getUpdatedTimeStamp());
		return pDto;
	}
	
	public Products getAll() {
		List<Product> prs = pRep.findAll();
		Products products = new Products();
		products.setProducts(prs);
		return products;
	}


	public void insertProduct(ProductDto pDto) {
		Product pr = new Product();
		pr.setName(pDto.getName());
		pr.setPrice(pDto.getPrice());
		pr.setInsertedTimeStamp(LocalDateTime.now());
		pRep.save(pr);
	}

	public void deleteProduct(Long id) {
		pRep.delete(id);
	}

	public void updateProduct(Long id, ProductDto pDto) {
		Product pr = new Product();
		pr = pRep.findOne(id);
		pr.setName(pDto.getName());
		pr.setPrice(pDto.getPrice());
		pr.setUpdatedTimeStamp(LocalDateTime.now());
		pRep.save(pr);
	}

	public Products getAllOrderByprice() {
		List<Product> prL = pRep.getproductByPrice();
		Products prs = new Products();
		prs.setProducts(prL);
		return prs;
	}

	public Products getAllBetweenPrice(float priceFrom, float priceTo) {
		List<Product> prL = pRep.getproductsBetweenPrice(priceFrom, priceTo);
		Products prs = new Products();
		prs.setProducts(prL);
		return prs;
	}

}
