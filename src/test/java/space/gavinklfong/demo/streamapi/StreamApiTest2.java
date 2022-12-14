package space.gavinklfong.demo.streamapi;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import space.gavinklfong.demo.streamapi.models.Customer;
import space.gavinklfong.demo.streamapi.models.Order;
import space.gavinklfong.demo.streamapi.models.Product;
import space.gavinklfong.demo.streamapi.repos.CustomerRepo;
import space.gavinklfong.demo.streamapi.repos.OrderRepo;
import space.gavinklfong.demo.streamapi.repos.ProductRepo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@DataJpaTest
public class StreamApiTest2 {

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private ProductRepo productRepo;

	@Test
	@DisplayName("Obtain a list of product with category = \"Books\" and price > 100")
	public void exercise1() {
		List<Product> all = productRepo.findAll();
		List<Product> book = all.stream().filter(e -> e.getCategory().equals("Books")).filter(e -> e.getPrice() > 100).collect(Collectors.toList());
		System.out.println(book);
	}

	@Test
	@DisplayName("Obtain a list of product with category = \"Books\" and price > 100 (using Predicate chaining for filter)")
	public void exercise1a() {
		List<Product> all = productRepo.findAll();
		Predicate<Product> pred1 =  e -> e.getCategory().equals("Books");
		Predicate<Product> pred2 =  e -> e.getPrice() > 100;
		List<Product> book = all.stream().filter(pred1.and(pred2)).collect(Collectors.toList());
		System.out.println(book);
	}

	@Test
	@DisplayName("Obtain a list of product with category = \"Books\" and price > 100 (using BiPredicate for filter)")
	public void exercise1b() {
		List<Product> all = productRepo.findAll();
		BiPredicate<Product, Product> pred =  (e, f) -> (e.getCategory().equals("Books") && f.getPrice() > 100);
		List<Product> book = all.stream().filter(e -> pred.test(e, e)).collect(Collectors.toList());
		System.out.println(book);
	}

	@Test
	@DisplayName("Obtain a list of order with product category = \"Baby\"")
	public void exercise2() {
		List<Order> all = orderRepo.findAll();
		List<Order> baby = all.stream().filter(e -> e.getProducts().stream().anyMatch(f -> f.getCategory().equals("Baby"))).collect(Collectors.toList());
		System.out.println(baby);
	}

	@Test
	@DisplayName("Obtain a list of product with category = “Toys” and then apply 10% discount\"")
	public void exercise3() {
		List<Product> all = productRepo.findAll();
		List<Product> toys = all.stream().filter(e -> e.getCategory().equals("Toys")).map(x -> x.withPrice(x.getPrice() * 0.9)).collect(Collectors.toList());
		toys.forEach(System.out::println);
	}

	@Test
	@DisplayName("Obtain a list of products ordered by customer of tier 2 between 01-Feb-2021 and 01-Apr-2021")
	public void exercise4() {
		List<Product> all = productRepo.findAll();

		List<Product> collect = all.stream().filter(e -> e.getOrders().stream().anyMatch(f -> f.getOrderDate().isAfter(LocalDate.of(2021, 2, 1)) && f.getOrderDate().isBefore(LocalDate.of(2021, 4, 1)))).collect(Collectors.toList());;
		collect.forEach(System.out::println);
	}

	@Test
	@DisplayName("Get the 3 cheapest products of \"Books\" category")
	public void exercise5() {
		List<Product> all = productRepo.findAll();
		List<Product> book = all.stream()
				.filter(e -> e.getCategory().equals("Books"))
				.sorted(Comparator.comparingDouble(Product::getPrice))
				.limit(3)
				.collect(Collectors.toList());

		book.forEach(System.out::println);

	}

	@Test
	@DisplayName("Get the 3 most recent placed order")
	public void exercise6() {
		List<Order> all = orderRepo.findAll();
		List<Order> collect = all.stream().sorted(Comparator.comparing(Order::getOrderDate).reversed()).limit(3).collect(Collectors.toList());
		collect.forEach(System.out::println);

	}

	@Test
	@DisplayName("Get a list of products which was ordered on 15-Mar-2021")
	public void exercise7() {
		List<Order> all = orderRepo.findAll();
		List<Product> collect = all.stream().filter(e -> e.getOrderDate().isEqual(LocalDate.of(2021, 3, 15))).flatMap(e -> e.getProducts().stream()).collect(Collectors.toList());
		collect.forEach(System.out::println);

	}

	@Test
	@DisplayName("Calculate the total lump of all orders placed in Feb 2021")
	public void exercise8() {

	}

	@Test
	@DisplayName("Calculate the total lump of all orders placed in Feb 2021 (using reduce with BiFunction)")
	public void exercise8a() {

	}

	@Test
	@DisplayName("Calculate the average price of all orders placed on 15-Mar-2021")
	public void exercise9() {
		List<Order> all = orderRepo.findAll();
		double asDouble = all.stream().filter(e -> e.getOrderDate().isEqual(LocalDate.of(2021, 3, 15))).flatMap(e -> e.getProducts().stream()).mapToDouble(Product::getPrice).average().getAsDouble();
		System.out.println(asDouble);
	}

	@Test
	@DisplayName("Obtain statistics summary of all products belong to \"Books\" category")
	public void exercise10() {
		List<Product> all = productRepo.findAll();
		DoubleSummaryStatistics books = all.stream().filter(e -> e.getCategory().equals("Books")).mapToDouble(Product::getPrice).summaryStatistics();
		System.out.println(books);
	}

	@Test
	@DisplayName("Obtain a mapping of order id and the order's product count")
	public void exercise11() {
		List<Order> all = orderRepo.findAll();
		Map<Long, Integer> collect = all.stream().collect(Collectors.toMap(Order::getId, order -> order.getProducts().size()));
		System.out.println(collect);
	}

	@Test
	@DisplayName("Obtain a data map of customer and list of orders")
	public void exercise12() {
		List<Order> all = orderRepo.findAll();
		Map<Customer, Long> collect = all.stream().map(a -> a.getCustomer()).collect(groupingBy(x -> x, counting()));
		collect.forEach((key, value) -> {
			System.out.print(key + "=" + value + " ");
		});;
	}

	@Test
	@DisplayName("Obtain a data map of customer_id and list of order_id(s)")
	public void exercise12a() {
		List<Order> all = orderRepo.findAll();
		HashMap<Long, List<Long>> collect = all.stream().collect(groupingBy(order -> order.getCustomer().getId(), HashMap::new, Collectors.mapping(Order::getId, Collectors.toList())));
		collect.forEach((key, value) -> {
			System.out.print(key + "=" + value + " ");
		});;

	}

	@Test
	@DisplayName("Obtain a data map with order and its total price")
	public void exercise13() {
		List<Order> all = orderRepo.findAll();
		Map<Order, Double> collect = all.stream().collect(Collectors.toMap(o -> o, o -> o.getProducts().stream().mapToDouble(p -> p.getPrice()).sum()));

		collect.forEach((key, value) -> {
			System.out.print(key + "=" + value + " ");
		});;
	}

	@Test
	@DisplayName("Obtain a data map with order and its total price (using reduce)")
	public void exercise13a() {

	}

	@Test
	@DisplayName("Obtain a data map of product name by category")
	public void exercise14() {

	}

	@Test
	@DisplayName("Get the most expensive product per category")
	void exercise15() {
		List<Product> all = productRepo.findAll();
		Map<String, Optional<Product>> collect = all.stream().collect(groupingBy(Product::getCategory, Collectors.maxBy(Comparator.comparing(Product::getPrice))));

	}
	
	@Test
	@DisplayName("Get the most expensive product (by name) per category")
	void exercise15a() {
		List<Product> all = productRepo.findAll();
		Map<String, String> collect = all.stream().collect(groupingBy(Product::getCategory,
				Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Product::getPrice)),
				optionalProduct -> optionalProduct.map(Product::getName).orElse(null))));
	}

}
