package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.InventoryItem;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    protected void prePersist(InventoryItem inventoryItem) throws Exception {
        if (inventoryItem.getQuantity() < 0){
            throw new InvalidParameterException("Inventory item quantity should be a positive number!");
        }
        if (inventoryItem.getUnitPrice() < 0){
            throw new InvalidParameterException("Inventory item price per unit should be a positive number!");
        }
        if (inventoryItem.getPackageVolume() < 0){
            throw new InvalidParameterException("Inventory item volume per package should be a positive number!");
        }
    }
}
