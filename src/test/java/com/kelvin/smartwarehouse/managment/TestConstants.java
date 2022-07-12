package com.kelvin.smartwarehouse.managment;

public class TestConstants {

    public final static String ORDERS_SCRIPTS = "/order";
    public final static String ORDERS_SCHEMA_SCRIPT = ORDERS_SCRIPTS + "/orders_schema.sql";
    public final static String IMPORT_ORDERS_SCRIPT = ORDERS_SCRIPTS + "/import_orders.sql";
    public final static String DELETE_ORDERS_STATEMENT = "delete from orders;";

    public final static String INVENTORY_ITEMS_SCRIPTS = "/inventory_item";
    public final static String INVENTORY_ITEMS_SCHEMA_SCRIPT = INVENTORY_ITEMS_SCRIPTS + "/inventory_items_schema.sql";
    public final static String IMPORT_INVENTORY_ITEMS_SCRIPT = INVENTORY_ITEMS_SCRIPTS + "/import_inventory_items.sql";
    public final static String DELETE_IMPORT_INVENTORY_STATEMENT = "delete from inventory_items;";

    public final static String ORDER_ITEMS_SCRIPTS = "/order_item";
    public final static String ORDER_ITEMS_SCHEMA_SCRIPT = ORDER_ITEMS_SCRIPTS +"/order_items_schema.sql";
    public final static String IMPORT_ORDER_ITEMS_SCRIPT = ORDER_ITEMS_SCRIPTS +"/import_order_items.sql";
    public final static String DELETE_ORDER_ITEMS_STATEMENT = "delete from order_items;";
}
