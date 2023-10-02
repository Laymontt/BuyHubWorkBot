package buyhub.prom.ua.buyhubworkbot.services;

import buyhub.prom.ua.buyhubworkbot.models.Product;
import buyhub.prom.ua.buyhubworkbot.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public String createProductInfo(String productName, String categories, String tags) {
        if (productRepository.findByName(productName).isPresent()) {
            return "Такой товар уже есть";
        }

        Product product = new Product();
        product.setName(productName);
        product.setCategories(categories.substring(1, categories.length() - 1));
        product.setTags(tags.substring(1, tags.length() - 1));

        productRepository.save(product);
        return "Информация о товаре добавлена в базу данных";
    }

    public Object getProductInfo(String productName) {
        Product product = productRepository.findByName(productName).orElseThrow();
        return product;
    }

    public Object getListProducts() {
        List<Product> products = (List<Product>) productRepository.findAll();
        List<String> productNames = new ArrayList<>();

        for (Product p : products) {
            productNames.add(p.getName());
        }

        String productNamesString = productNames.toString();
        return productNamesString.substring(1, productNamesString.length() - 1);
    }

    public Object deleteProductInfo(long id) {
        if (productRepository.findById(id).isEmpty())
            return "Информации о данном товаре нет.";
        Product product = productRepository.findById(id).orElseThrow();
        productRepository.deleteById(id);
        return product.getName();
    }

    public Object editProductInfo(String[] textArr) {
        String id = textArr[0];

        if (productRepository.findById(Long.parseLong(id)).isEmpty())
            return "Информации о данном товаре нету";

        String parameter = textArr[1];
        Product product = productRepository.findById(Long.parseLong(id)).orElseThrow();
        switch (parameter) {
            case "Название":
                String newName = textArr[2];
                product.setName(newName);
                productRepository.save(product);
                return "Название товара успешно изменено.\nНовое название товара: " + product.getName();
            case "Категории":
                List<String> categories = new ArrayList<>();
                for (int i = 2; i < textArr.length; i++)
                    categories.add(textArr[i]);
                String categoriesString = categories.toString();
                categoriesString = categoriesString.substring(1, categoriesString.length() - 1);
                product.setCategories(categoriesString);
                productRepository.save(product);
                return "Категории товара успешно изменены.\nНовые категории товара: " + product.getCategories();
            case "Теги":
                List<String> tags = new ArrayList<>();
                for (int i = 2; i < textArr.length; i++)
                    tags.add(textArr[i]);
                String tagsString = tags.toString();
                tagsString = tagsString.substring(1, tagsString.length() - 1);
                product.setTags(tagsString);
                productRepository.save(product);
                return "Теги товара успешно изменены.\nНовые теги товара: " + product.getTags();
            default:
                return "Выберите один из следующих параметров:\n\n" +
                        "1. Название\n" +
                        "2. Категории\n" +
                        "3. Теги";
        }
    }
}