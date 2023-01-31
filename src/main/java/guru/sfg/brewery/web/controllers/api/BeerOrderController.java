package guru.sfg.brewery.web.controllers.api;

import guru.sfg.brewery.security.permissions.BeerOrderCreatePermission;
import guru.sfg.brewery.security.permissions.BeerOrderReadPermission;
import guru.sfg.brewery.services.BeerOrderService;
import guru.sfg.brewery.web.model.BeerOrderDto;
import guru.sfg.brewery.web.model.BeerOrderPagedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/v1/customers/{customerId}/")
@RestController
public class BeerOrderController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final BeerOrderService beerOrderService;

    public BeerOrderController(BeerOrderService beerOrderService) {
        this.beerOrderService = beerOrderService;
    }


    @PreAuthorize("hasAuthority('order.read') OR hasAuthority('customer.order.read') AND @beerOrderAuthenticationManager.customerIdMatches(authentication, #customerId)")
    //order.read ist für admin user - customer.order.read ist für customer user -> siehe DefaultBreweryLoader Definitionen
    @GetMapping("orders")
    public BeerOrderPagedList listOrders(@PathVariable("customerId")UUID customerId,
                                         @RequestParam(value = "pageNumber", required = false)Integer pageNumber,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize){
    if(pageNumber == null || pageNumber < 0){
        pageNumber =DEFAULT_PAGE_NUMBER;
    }
    if(pageSize == null || pageSize < 0){
        pageSize = DEFAULT_PAGE_SIZE;
    }
    return beerOrderService.listOrders(customerId, PageRequest.of(pageNumber, pageSize));
    }

    @BeerOrderCreatePermission
//    @PreAuthorize("hasAuthority('order.create') OR hasAuthority('customer.order.create') AND @beerOrderAuthenticationManager.customerIdMatches(authentication, #customerId)")
    @PostMapping("orders")
    @ResponseStatus(HttpStatus.CREATED)
    public BeerOrderDto placeOrder(@PathVariable("customerId")UUID customerId, @RequestBody BeerOrderDto beerOrderDto){
        log.debug("Customer Id: " + customerId);

        return beerOrderService.placeOrder(customerId, beerOrderDto);
    }

    @BeerOrderReadPermission
//    @PreAuthorize("hasAuthority('order.read') OR hasAuthority('customer.order.read') AND @beerOrderAuthenticationManager.customerIdMatches(authentication, #customerId)")
//    order.read ist für admin user - customer.order.read ist für customer user -> siehe DefaultBreweryLoader Definitionen
    @GetMapping("orders/{orderId}")
    public BeerOrderDto getOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        return beerOrderService.getOrderById(customerId, orderId);
    }

    @PutMapping("/orders/{orderId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        beerOrderService.pickupOrder(customerId, orderId);
    }







}
