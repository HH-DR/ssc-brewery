/*
 *  Copyright 2020 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package guru.sfg.brewery.repositories;

import guru.sfg.brewery.domain.BeerOrder;
import guru.sfg.brewery.domain.Customer;
import guru.sfg.brewery.domain.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

/**
 * Created by jt on 2019-01-26.
 */
public interface BeerOrderRepository  extends JpaRepository<BeerOrder, UUID> {

    Page<BeerOrder> findAllByCustomer(Customer customer, Pageable pageable);

    List<BeerOrder> findAllByOrderStatus(OrderStatusEnum orderStatusEnum);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    BeerOrder findOneById(UUID id);

//    Das ist keine Standard Implementierung von JPA, deswegen muss die Query definiert werden
//    1. SELECT order FROM WHERE ist normales SQL
//    2. order.id =?1 bezieht sich auf den Parameter 1 der Methode - also die orderId
//    3. :#{hasAuthority(('order.read'))} Der : sagt hibernate, dass die Methode hasAuthority der Spring Expression Language aufgerufen werden soll
//    Ohne den : würde hibernate den folgenden Text als String interpretieren
//
    @Query("SELECT order FROM BeerOrder order WHERE order.id =?1 " +
            "AND (true = :#{hasAuthority('order.read')} OR order.customer.id = ?#{principal?.customer?.id})")
    BeerOrder findOrderBySecure(UUID orderId);
}
