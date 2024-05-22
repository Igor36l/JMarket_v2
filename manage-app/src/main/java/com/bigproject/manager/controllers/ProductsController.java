package com.bigproject.manager.controllers;

import com.bigproject.manager.client.reviewClient.exception.BadRequestException;
import com.bigproject.manager.client.favouriteClient.FavouriteProductClient;
import com.bigproject.manager.client.productClient.ProductRestClient;
import com.bigproject.manager.controllers.payload.NewProductPayload;
import com.bigproject.manager.entity.FavouriteProduct;
import com.bigproject.manager.entity.Product;
import com.bigproject.manager.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/products")
public class ProductsController {

    private final ProductRestClient productRestClient;

    private final FavouriteProductClient favouriteProductClient;

    @GetMapping("favourites")
    public String getFavouriteProductsPage(Model model,
                                                 @RequestParam(name = "filter", required = false) String filter) {
        model.addAttribute("filter", filter);

        FavouriteProduct[] favouriteProducts = this.favouriteProductClient.findFavouriteProducts();

        List<Integer> list = Stream.of(favouriteProducts).map(FavouriteProduct::productId).toList();
        List<Product> list1 = productRestClient.findAllProducts(filter).stream().toList();

        List<Product> list2 = list1.stream().filter(a -> list.contains(a.id())).toList();


        model.addAttribute("products", list2);
        return  "catalogue/products/favourites";
    }


    @GetMapping("list")
    public String getProductsList(Model model, @RequestParam(name = "filter", required = false) String filter) {
        model.addAttribute("products", this.productRestClient.findAllProducts(filter));
        model.addAttribute("filter", filter);
        return "catalogue/products/list";
    }

    @GetMapping("create")
    public String getNewProductPage() {
        return "catalogue/products/new_product";
    }

    @PostMapping("create")
    public String createProduct(NewProductPayload payload, Model model) {
       try {
           Product product = productRestClient.createProduct(payload.title(), payload.details());
           return "redirect:/catalogue/products/%d".formatted(product.id());
       } catch (BadRequestException exception) {
           model.addAttribute("payload", payload);
           model.addAttribute("errors", exception.getErrors());
           return "catalogue/products/new_product";
       }
    }


}
