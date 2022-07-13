package com.kelvin.smartwarehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelvin.smartwarehouse.exception.EntityWithIdNotFoundException;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.InventoryItem;
import com.kelvin.smartwarehouse.model.OrderItem;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDER_ITEMS_URL;
import static com.kelvin.smartwarehouse.managment.TestConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ORDERS_SCHEMA_SCRIPT)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = INVENTORY_ITEMS_SCHEMA_SCRIPT)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ORDER_ITEMS_SCHEMA_SCRIPT)
public class OrderItemApiTest {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;


    static final String apiUrl = ORDER_ITEMS_URL;

    @Autowired
    public OrderItemApiTest(ObjectMapper mapper, MockMvc mockMvc) {
        this.objectMapper = mapper;
        this.mockMvc = mockMvc;
    }

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDERS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDER_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDERS_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDER_ITEMS_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAll_thenOkAndShouldReturnArray() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "15"))
                .andExpect(jsonPath("$.[0].uuid", is("a169f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.[0].itemUuid", is("051191d4-4eba-48ca-9a8c-19076eb7f669")))
                .andExpect(jsonPath("$.[0].orderUuid", is("f72a8cde-412a-4a56-9974-b11d8f8da684")))
                .andExpect(jsonPath("$.[0].quantity", is(100)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDERS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDER_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDERS_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDER_ITEMS_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithFilter_thenOkAndShouldReturnArrayFiltered() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?obj.orderUuid=e8320e39-f185-4044-87df-8a7de39c0058")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(3)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "3"))
                .andExpect(jsonPath("$.[0].uuid", is("a4e9f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.[0].itemUuid", is("b2e9f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.[0].orderUuid", is("e8320e39-f185-4044-87df-8a7de39c0058")))
                .andExpect(jsonPath("$.[0].quantity", is(35)));

    }

    @Test
    @Order(2)
    void givenEmptyTable_whenGetAll_thenOkAndShouldReturnEmptyArray() throws Exception {
        //given
        //we have the table empty

        //when
        this.mockMvc.perform(
                        get(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));

    }

    @Test
    @Order(2)
    void givenOrderItem_whenPost_thenShouldPersistOrderItem() throws Exception {
        //given
        OrderItem orderItem = buildOrderItem();

        String jsonBody = objectMapper.writeValueAsString(orderItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(notNullValue())))
                .andExpect(jsonPath("$.itemUuid", is(orderItem.getItemUuid())))
                .andExpect(jsonPath("$.orderUuid", is(orderItem.getOrderUuid())))
                .andExpect(jsonPath("$.quantity", is(orderItem.getQuantity())));

    }

    private OrderItem buildOrderItem() {
        OrderItem orderItem = new OrderItem();

        orderItem.setItemUuid("c41fbe24-5e1a-41d6-8bdd-de9a0c3df1e7");
        orderItem.setOrderUuid("9fe2e517-c135-4f3e-a1c2-705e5b59a4f7");
        orderItem.setQuantity(37);

        return orderItem;
    }

    @Test
    @Order(2)
    void givenOrderItemWithoutItemUuid_whenPost_thenShouldReturn4xxClientError() throws Exception {
        //given
        OrderItem orderItem = buildOrderItem();
        orderItem.setItemUuid(null);

        String jsonBody = objectMapper.writeValueAsString(orderItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Order item uuid of item is required!")));

    }

    @Test
    @Order(2)
    void givenOrderItemWithoutOrderUuid_whenPost_thenShouldReturn4xxClientError() throws Exception {
        //given
        OrderItem orderItem = buildOrderItem();
        orderItem.setOrderUuid(null);

        String jsonBody = objectMapper.writeValueAsString(orderItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Order item order uuid is required!")));

    }

    @Test
    @Order(2)
    void givenInventoryItemWithNegativeQuantity_whenPost_thenShouldReturn4xxClientError() throws Exception {
        //given
        OrderItem orderItem = buildOrderItem();
        orderItem.setQuantity(-25);

        String jsonBody = objectMapper.writeValueAsString(orderItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Order item quantity should be a positive number!")));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDERS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDER_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDERS_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDER_ITEMS_STATEMENT)
    })
    void givenSeedDataFromImportSqlAndId_whenGetById_thenOkAndShouldReturnOrderItemWithGivenId() throws Exception {

        //given
        //the data imported from import_order_items.sql
        String id = "a109f0ed-1364-45e6-9d3a-5cc5456e75f9";

        //when
        this.mockMvc.perform(
                        get(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("a109f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.itemUuid", is("051191d4-4eba-48ca-9a8c-19076eb7f669")))
                .andExpect(jsonPath("$.orderUuid", is("044ad906-ace4-4963-9ef7-53444f5584f8")))
                .andExpect(jsonPath("$.quantity", is(51)));
    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDERS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDER_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDERS_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDER_ITEMS_STATEMENT)
    })
    void givenEmptyListAndId_whenGetById_thenShouldReturn4xxClientError() throws Exception {

        //given
        //the data imported from import_order_items.sql
        String id = "IdNotPresentInDb";

        //when
        this.mockMvc.perform(
                        get(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNoContent())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityWithIdNotFoundException))
                .andExpect(jsonPath("$.message", is(String.format("Order item with id [%s] doesn't exist in database!", id))));
    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDERS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDER_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDERS_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDER_ITEMS_STATEMENT)
    })
    void givenSeedDataFromImportSqlAndId_whenUpdate_thenOkStatusAndShouldUpdateOrderItem() throws Exception {

        //given
        String id = "a6e9f0ed-1364-45e6-9d3a-5cc5456e75f9";
        String requestBody = "{\n" +
                "    \"uuid\" : \"a6e9f0ed-1364-45e6-9d3a-5cc5456e75f9\",\n" +
                "    \"itemUuid\" : \"9fe2e517-c135-4f3e-a1c2-705e5b59a4f7\",\n" +
                "    \"orderUuid\" : \"8f9093a0-5023-45fd-b594-0dc6379fe4f9\",\n" +
                "    \"quantity\" : 77 \n" +
                "}";

        //when
        this.mockMvc.perform(
                        put(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("a6e9f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.itemUuid", is("9fe2e517-c135-4f3e-a1c2-705e5b59a4f7")))
                .andExpect(jsonPath("$.orderUuid", is("8f9093a0-5023-45fd-b594-0dc6379fe4f9")))
                .andExpect(jsonPath("$.quantity", is(77)));

    }

    @Test
    @Order(2)
    void givenEmptyListAndIdAndOrderItem_whenUpdate_thenOkStatusAndShouldCreateNewInventoryItem() throws Exception {

        //given
        String id = "b5e9f0ed-1364-45e6-9d3a-5cc5456e75f9";
        String requestBody = "{\n" +
                "    \"uuid\" : \"b5e9f0ed-1364-45e6-9d3a-5cc5456e75f9\",\n" +
                "    \"itemUuid\" : \"9fe2e517-c135-4f3e-a1c2-705e5b59a4f7\",\n" +
                "    \"orderUuid\" : \"8f9093a0-5023-45fd-b594-0dc6379fe4f9\",\n" +
                "    \"quantity\" : 77 \n" +
                "}";

        //when
        this.mockMvc.perform(
                        put(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("b5e9f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.itemUuid", is("9fe2e517-c135-4f3e-a1c2-705e5b59a4f7")))
                .andExpect(jsonPath("$.orderUuid", is("8f9093a0-5023-45fd-b594-0dc6379fe4f9")))
                .andExpect(jsonPath("$.quantity", is(77)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDERS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_ORDER_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDERS_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_ORDER_ITEMS_STATEMENT)
    })
    void givenSeedDataFromImportSqlAndId_whenDelete_thenNotFoundStatusAndShouldDeleteInventoryItem() throws Exception {

        //given
        String id = "a4e9f0ed-1364-45e6-9d3a-5cc5456e75f9";

        //when
        this.mockMvc.perform(
                        delete(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNoContent());

        this.mockMvc.perform(
                        get(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityWithIdNotFoundException))
                .andExpect(jsonPath("$.message", is(String.format("Order item with id [%s] doesn't exist in database!", id))));

    }

    @Test
    @Order(2)
    void givenEmptyOrdersListAndId_whenDelete_thenNotFoundStatus() throws Exception {

        //given
        String id = "IdNotPresentInDb";

        //when
        this.mockMvc.perform(
                        delete(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNoContent());

        this.mockMvc.perform(
                        get(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityWithIdNotFoundException))
                .andExpect(jsonPath("$.message", is(String.format("Order item with id [%s] doesn't exist in database!", id))));

    }
}


