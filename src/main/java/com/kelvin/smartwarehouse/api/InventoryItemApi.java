package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.InventoryItem;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static com.kelvin.smartwarehouse.management.AppConstants.INVENTORY_ITEMS_URL;

@RestController
@RequestMapping(INVENTORY_ITEMS_URL)
public class InventoryItemApi extends BaseApi<InventoryItem> {

    public InventoryItemApi() {
        super(InventoryItem.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "name asc";
    }

    @Override
    protected List<Predicate> getFilters(CriteriaBuilder criteriaBuilder, Root<InventoryItem> root){
        List<Predicate> predicates = new ArrayList<>();

        if (nn("like.name")) {
            predicates.add(criteriaBuilder.like(root.get("name"), likeParam("like.name")));
        }

        buildIntegerFieldFilters("quantity", criteriaBuilder, root, predicates);

        buildDoubleFieldFilters("unitPrice", criteriaBuilder, root, predicates);

        buildDoubleFieldFilters("packageVolume", criteriaBuilder, root, predicates);

        return predicates;
    }

    @Override
    protected void prePersist(InventoryItem inventoryItem) throws Exception {
        if (inventoryItem.getQuantity() < 1){
            throw new InvalidParameterException("Inventory item quantity should be a positive number!");
        }
        if (inventoryItem.getUnitPrice() < 1){
            throw new InvalidParameterException("Inventory item price per unit should be a positive number!");
        }
        if (inventoryItem.getPackageVolume() < 1){
            throw new InvalidParameterException("Inventory item volume per package should be a positive number!");
        }
    }
}
