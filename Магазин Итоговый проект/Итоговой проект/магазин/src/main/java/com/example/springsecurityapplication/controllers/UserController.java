package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.enumm.Status;
import com.example.springsecurityapplication.models.Cart;
import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.models.Person;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.CartRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.repositories.PersonRepository;
import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import com.example.springsecurityapplication.services.OrderService;
import com.example.springsecurityapplication.services.PersonService;
import com.example.springsecurityapplication.services.ProductService;
import com.example.springsecurityapplication.util.PersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("MVCPathVariableInspection")
//@RequestMapping("/user")
@Controller

public class UserController {

    private final OrderRepository orderRepository;

    private final CartRepository cartRepository;
    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final PersonService personService;

    private final PersonValidator personValidator;
    private final PersonRepository personRepository;

    @Autowired
    public UserController(PersonRepository personRepository, PersonValidator personValidator, OrderRepository orderRepository, CartRepository cartRepository, ProductService productService,
                          OrderService orderService, ProductRepository productRepository, PersonService personService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.personService = personService;
        this.personValidator = personValidator;
        this.personRepository = personRepository;
    }




    @GetMapping("/index")
    public String index(Model model){


        // Получае объект аутентификации - > c помощью SecurityContextHolder обращаемся к контексту и на нем вызываем метод аутентификации. По сути из потока для текущего пользователя мы получаем объект, который был положен в сессию после аутентификации пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Преобразовываем объект аутентификации в специальный объект класса по работе с пользователями
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        String role = personDetails.getPerson().getRole();

        if(role.equals("ROLE_ADMIN")){
            return "redirect:/admin";
        }
        if(role.equals("ROLE_SELLER")){
            return "redirect:/seller";
        }
        model.addAttribute("products", productService.getAllProduct());
        return "user/index";
    }
    @GetMapping("/user/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model){
        model.addAttribute("product", productService.getProductId(id));
        return "user/infoProduct";
    }

    // Добавить товар в корзину
    @GetMapping("/cart/add/{id}")
    public String addProductInCart(@PathVariable("id") int id, Model model){
        Product product = productService.getProductId(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int id_person = personDetails.getPerson().getId();
        Cart cart = new Cart(id_person, product.getId());
        cartRepository.save(cart);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int id_person = personDetails.getPerson().getId();
        List<Cart> cartList = cartRepository.findByPersonId(id_person);
        List<Product> productList = new ArrayList<>();
        for (Cart cart: cartList) {
            productList.add(productService.getProductId(cart.getProductId()));
        }

        float price = 0;
        for (Product product: productList) {
            price += product.getPrice();
        }

        model.addAttribute("price", price);
        model.addAttribute("cart_product", productList);
        return "user/cart";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteProductFromCart(Model model, @PathVariable("id") int id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int id_person = personDetails.getPerson().getId();
        cartRepository.deleteCartById(id, id_person);
        return "redirect:/cart";
    }

//    @PostMapping("/search")
//    public String productSearch(@RequestParam("search") String search, @RequestParam("ot") String ot, @RequestParam("do") String Do, @RequestParam(value = "price", required = false, defaultValue = "") String price, @RequestParam(value = "category", required = false, defaultValue = "") String category, Model model){
//        System.out.println("lf");
//        return "redirect:/product";
//    }

    @GetMapping("/order/create")
    public String createOrder(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int id_person = personDetails.getPerson().getId();
        List<Cart> cartList = cartRepository.findByPersonId(id_person);
        List<Product> productList = new ArrayList<>();
        for (Cart cart: cartList) {
            productList.add(productService.getProductId(cart.getProductId()));
        }

        String uuid = UUID.randomUUID().toString();

        for (Product product: productList) {
            Order newOrder = new Order(uuid, 1, product.getPrice(), Status.Оформлен, product, personDetails.getPerson());
            orderRepository.save(newOrder);
            cartRepository.deleteCartById(product.getId(), id_person);
        }
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String ordersUser(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        List<Order> orderList = orderRepository.findByPerson(personDetails.getPerson());
        model.addAttribute("orders", orderList);
        return "/user/orders";
    }



    @PostMapping("/orderCansel/{id}")
    public String updateOrderCansel(@ModelAttribute("orders")  Order order, @PathVariable("id") int id){
        Order order_status = orderService.getOrderById(id);
        orderService.updateOrderCansel(order_status);
        return "redirect:/orders";
    }


    @PostMapping("/user/searchs")
    public String productSearch(@RequestParam("search") String search, @RequestParam("ot") String ot, @RequestParam("do") String Do,
    @RequestParam(value = "price", required = false, defaultValue = "") String price, @RequestParam(value = "category", required = false,
            defaultValue = "") String category, Model model){
        System.out.println(search);
        System.out.println(ot);
        System.out.println(Do);
        System.out.println(price);
        System.out.println(category);
        // Если диапазон цен от и до не пустой
        if(!ot.isEmpty() & !Do.isEmpty()) {
            // Если сортировка по цене выбрана
            if (!price.isEmpty()) {
                // Если в качестве сортировки выбрана сортировкам по возрастанию
                if (price.equals("sorted_by_ascending_price")) {
                    // Если категория товара не пустая
                    if (!category.isEmpty()) {
                        // Если категория равная мебели
                        if (category.equals("furniture")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPrice(search.toLowerCase(),
                                    Float.parseFloat(ot), Float.parseFloat(Do), 1));
                            // Если категория равная бытовой техники
                        } else if (category.equals("appliances")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPrice(search.toLowerCase(),
                                    Float.parseFloat(ot), Float.parseFloat(Do), 2));
                            // Если категория равная одежде
                        } else if (category.equals("clothes")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPrice(search.toLowerCase(),
                                    Float.parseFloat(ot), Float.parseFloat(Do), 3));
                        }
                        // Если категория не выбрана
                    } else {
                        model.addAttribute("search_product", productRepository.findByTitleOrderByPrice(search.toLowerCase(), Float.parseFloat(ot),
                                Float.parseFloat(Do)));
                    }

                    // Если в качестве сортировки выбрана сортировкам по убыванию
                } else if (price.equals("sorted_by_descending_price")) {

                    // Если категория не пустая
                    if (!category.isEmpty()) {
                        // Если категория равная мебели
                        if (category.equals("furniture")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 1));
                            // Если категория равная бытовой техники
                        } else if (category.equals("appliances")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 2));
                            // Если категория равная одежде
                        } else if (category.equals("clothes")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do), 3));
                        }
                        // Если категория не выбрана
                    }
                    else {
                        model.addAttribute("search_product", productRepository.findByTitleOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do)));
                    }
                }
            }
            else {
                model.addAttribute("search_product", productRepository.findByTitleAndPriceGreaterThanEqualAndPriceLessThan(search.toLowerCase(), Float.parseFloat(ot), Float.parseFloat(Do)));
            }
        } else {
            model.addAttribute("search_product",productRepository.findByTitleContainingIgnoreCase(search));
        }
        model.addAttribute("value_search", search);
        model.addAttribute("value_price_ot", ot);
        model.addAttribute("value_price_do", Do);
        model.addAttribute("products", productService.getAllProduct());
        return "/user/index";
    }
////смена пользовательского пароля
//    @GetMapping("/password/change")
//    public String changePassword(Model model){
//        model.addAttribute("person", new Person());
//        return "/user/rePassword";
//    }
//
//    @PostMapping("/password/change")
//    public String changePassword(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult){
//
//        personValidator.findUser(person, bindingResult);
//        if(bindingResult.hasErrors()){
//            return "/user/rePassword";
//        }
//
//        Person person_db = personService.getPersonFindByLogin(person);
//
//        int id = person_db.getId();
//        String password = person.getPassword();
//        personService.changePassword(id, password);
//
//        return "redirect:/index";
//    }



//maxs
//    @GetMapping("/user/password/user")
//    public String changePasswordPersonal(Model model){
//        model.addAttribute("person", new Person());
//        model.addAttribute("login", SecurityContextHolder.getContext().getAuthentication().getName());
//        return "user/rePassword";
//    }
//
//    @PostMapping("/user/password/user")
//    public String changePasswordPersonal(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult, Model model){
//        personValidator.findUser(person, bindingResult);
//        if(bindingResult.hasErrors()){
//            model.addAttribute("login", SecurityContextHolder.getContext().getAuthentication().getName());
//            return "user/rePassword";
//        }
//
//        Person person_db = personService.getPersonFindByLogin(person);
//        int id = person_db.getId();
//        String password = person.getPassword();
//        personService.changePassword(id, password);
//
//        return "redirect:/index";
//    }

//    @GetMapping("/user/updatePassword")
////    @PreAuthorize("hasRole('READ_PRIVILEGE')")
//    public String changeUserPassword(Model model) {
//        model.addAttribute("persons", new Person());
//        return "/user/updatePassword";
//    }
//    @PostMapping("/user/updatePassword")
//    @PreAuthorize("hasRole('READ_PRIVILEGE')")
//    public String updatePassword(@ModelAttribute("person")  Person person, BindingResult bindingResult){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
//        int id_person = personDetails.getPerson().getId();
//        Person person_db = personService.getPersonFindByLogin(person);
//        int id = person_db.getId().getPassword();
//     //   String password= person.getPassword();
//        personService.updatePassword(id,password);
//        return "redirect:/user/updatePassword";
//    }



    // Метод возвращает страницу с формой редактирования пользователя и помещает в модель объект редактируемого пользователя по id
//    @GetMapping("/Password/update/{id}")
//    public String editPerson(@PathVariable("id")int id, Model model){
//       model.addAttribute("editPersons", personService.getPersonById(id));
//        //      model.addAttribute("passwords", personService.getPasswordById(password));
//        return "user/updatePassword";
//    }
//
//    // Метод принимает объект с формы и обновляет пользователя
//    @PostMapping("/Password/update/{id}")
//    public String editPerson(@ModelAttribute("editPersons") @Valid Person person, BindingResult bindingResult, @PathVariable("id") int id){
//        if(bindingResult.hasErrors()){
//            return "/user/updatePassword";
//        }
//        personService.updatePassword(id, person);
//        return "redirect:/index";
//    }
}
