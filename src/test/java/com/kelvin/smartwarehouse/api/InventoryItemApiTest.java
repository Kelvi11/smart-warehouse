package com.kelvin.smartwarehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelvin.smartwarehouse.exception.EntityWithIdNotFoundException;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.InventoryItem;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static com.kelvin.smartwarehouse.management.AppConstants.INVENTORY_ITEMS_URL;
import static com.kelvin.smartwarehouse.managment.TestConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = INVENTORY_ITEMS_SCHEMA_SCRIPT)
public class InventoryItemApiTest {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    static final String importRecordsScript = IMPORT_INVENTORY_ITEMS_SCRIPT;
    static final String deleteStatement = DELETE_IMPORT_INVENTORY_STATEMENT;
    
    static final String apiUrl = INVENTORY_ITEMS_URL;

    @Autowired
    public InventoryItemApiTest(ObjectMapper mapper, MockMvc mockMvc) {
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
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = importRecordsScript),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = deleteStatement)
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
                .andExpect(header().string("listSize", "100"))
                .andExpect(jsonPath("$.[0].uuid", is("9d2fbdfb-e835-4f4e-aca1-071a66d9845c")))
                .andExpect(jsonPath("$.[0].name", is("Alberta Beardtongue")))
                .andExpect(jsonPath("$.[0].quantity", is(8661)))
                .andExpect(jsonPath("$.[0].unitPrice", is(7798.75)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9680.39)));

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
    void givenInventoryItem_whenPost_thenShouldPersistInventoryItem() throws Exception {
        //given
        InventoryItem inventoryItem = buildInventoryItem();

        String jsonBody = objectMapper.writeValueAsString(inventoryItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(inventoryItem.getName())))
                .andExpect(jsonPath("$.quantity", is(inventoryItem.getQuantity())))
                .andExpect(jsonPath("$.unitPrice", is(inventoryItem.getUnitPrice())))
                .andExpect(jsonPath("$.packageVolume", is(inventoryItem.getPackageVolume())));

    }

    private InventoryItem buildInventoryItem() {
        InventoryItem inventoryItem = new InventoryItem();
        
        inventoryItem.setName("Herbal Tea");
        inventoryItem.setQuantity(15);
        inventoryItem.setUnitPrice(1.20);
        inventoryItem.setPackageVolume(3.00);
        
        return inventoryItem;
    }

    @Test
    @Order(2)
    void givenInventoryItemWithNegativeQuantity_whenPost_thenShouldReturn4xxClientError() throws Exception {
        //given
        InventoryItem inventoryItem = buildInventoryItem();
        inventoryItem.setQuantity(-15);

        String jsonBody = objectMapper.writeValueAsString(inventoryItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Inventory item quantity should be a positive number!")));

    }

    @Test
    @Order(2)
    void givenInventoryItemWithNegativeUnitPrice_whenPost_thenShouldReturn4xxClientError() throws Exception {
        //given
        InventoryItem inventoryItem = buildInventoryItem();
        inventoryItem.setUnitPrice(-56.05);

        String jsonBody = objectMapper.writeValueAsString(inventoryItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Inventory item price per unit should be a positive number!")));

    }

    @Test
    @Order(2)
    void givenInventoryItemWithNegativePackageVolume_whenPost_thenShouldReturn4xxClientError() throws Exception {
        //given
        InventoryItem inventoryItem = buildInventoryItem();
        inventoryItem.setPackageVolume(-87.68);

        String jsonBody = objectMapper.writeValueAsString(inventoryItem);

        //when
        this.mockMvc.perform(
                        post(apiUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Inventory item volume per package should be a positive number!")));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = importRecordsScript),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = deleteStatement)
    })
    void givenSeedDataFromImportSqlAndId_whenGetById_thenOkAndShouldReturnInventoryItemWithGivenId() throws Exception {

        //given
        //the data imported from import_order_items.sql
        String id = "d35254fb-497f-4542-9cb3-5c7690f9901f";

        //when
        this.mockMvc.perform(
                        get(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("d35254fb-497f-4542-9cb3-5c7690f9901f")))
                .andExpect(jsonPath("$.name", is("Texan Hogplum")))
                .andExpect(jsonPath("$.quantity", is(8656)))
                .andExpect(jsonPath("$.unitPrice", is(8060.56)))
                .andExpect(jsonPath("$.packageVolume", is(8462.38)));
    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = importRecordsScript),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = deleteStatement)
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
                .andExpect(jsonPath("$.message", is(String.format("Inventory item with id [%s] doesn't exist in database!", id))));
    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = importRecordsScript),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = deleteStatement)
    })
    void givenSeedDataFromImportSqlAndId_whenUpdate_thenOkStatusAndShouldUpdateInventoryItem() throws Exception {

        //given
        String id = "b43ec1ac-fa8b-49d0-b947-c1e349496edf";
        String requestBody = "{\n" +
                "    \"uuid\" : \"b43ec1ac-fa8b-49d0-b947-c1e349496edf\",\n" +
                "    \"name\" : \"Texan Hogplum Updated\",\n" +
                "    \"quantity\" : 8656, \n" +
                "    \"unitPrice\" : 8060.56, \n" +
                "    \"packageVolume\" : 8500.00 \n" +
                "}";

        //when
        this.mockMvc.perform(
                        put(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("b43ec1ac-fa8b-49d0-b947-c1e349496edf")))
                .andExpect(jsonPath("$.name", is("Texan Hogplum Updated")))
                .andExpect(jsonPath("$.quantity", is(8656)))
                .andExpect(jsonPath("$.unitPrice", is(8060.56)))
                .andExpect(jsonPath("$.packageVolume", is(8500.00)));

    }

    @Test
    @Order(2)
    void givenEmptyListAndIdAndInventoryItem_whenUpdate_thenOkStatusAndShouldCreateNewInventoryItem() throws Exception {

        //given
        String id = "a43ec1ac-fa8b-49d0-b947-c1e349496edf";
        String requestBody = "{\n" +
                "    \"uuid\" : \"a43ec1ac-fa8b-49d0-b947-c1e349496edf\",\n" +
                "    \"name\" : \"Herbal Tea\",\n" +
                "    \"quantity\" : 8656, \n" +
                "    \"unitPrice\" : 8060.56, \n" +
                "    \"packageVolume\" : 8500.00 \n" +
                "}";

        //when
        this.mockMvc.perform(
                        put(apiUrl + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("a43ec1ac-fa8b-49d0-b947-c1e349496edf")))
                .andExpect(jsonPath("$.name", is("Herbal Tea")))
                .andExpect(jsonPath("$.quantity", is(8656)))
                .andExpect(jsonPath("$.unitPrice", is(8060.56)))
                .andExpect(jsonPath("$.packageVolume", is(8500.00)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = importRecordsScript),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = deleteStatement)
    })
    void givenSeedDataFromImportSqlAndId_whenDelete_thenNotFoundStatusAndShouldDeleteInventoryItem() throws Exception {

        //given
        String id = "8af18e2f-95b5-4569-abde-ebc39746fc99";

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
                .andExpect(jsonPath("$.message", is(String.format("Inventory item with id [%s] doesn't exist in database!", id))));

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
                .andExpect(jsonPath("$.message", is(String.format("Inventory item with id [%s] doesn't exist in database!", id))));

    }

    //filters
    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLikeNameFilter_thenOkAndShouldReturnArrayFilteredByLikeName() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?like.name=B")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "15"))
                .andExpect(jsonPath("$.[0].uuid", is("9d2fbdfb-e835-4f4e-aca1-071a66d9845c")))
                .andExpect(jsonPath("$.[0].name", is("Alberta Beardtongue")))
                .andExpect(jsonPath("$.[0].quantity", is(8661)))
                .andExpect(jsonPath("$.[0].unitPrice", is(7798.75)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9680.39)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithEqQuantityFilter_thenOkAndShouldReturnArrayFilteredByQuantityEquality() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?eq.quantity=8958")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "1"))
                .andExpect(jsonPath("$.[0].uuid", is("90b60fd8-2763-4095-aee7-80d326f6a790")))
                .andExpect(jsonPath("$.[0].name", is("Heller's Grape")))
                .andExpect(jsonPath("$.[0].quantity", is(8958)))
                .andExpect(jsonPath("$.[0].unitPrice", is(1944.75)))
                .andExpect(jsonPath("$.[0].packageVolume", is(4050.26)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithGtQuantityFilter_thenOkAndShouldReturnArrayFilteredByGtQuantity() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?gt.quantity=8131")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "21"))
                .andExpect(jsonPath("$.[0].uuid", is("9d2fbdfb-e835-4f4e-aca1-071a66d9845c")))
                .andExpect(jsonPath("$.[0].name", is("Alberta Beardtongue")))
                .andExpect(jsonPath("$.[0].quantity", is(8661)))
                .andExpect(jsonPath("$.[0].unitPrice", is(7798.75)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9680.39)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithGeQuantityFilter_thenOkAndShouldReturnArrayFilteredByGeQuantity() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?ge.quantity=6534")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "38"))
                .andExpect(jsonPath("$.[0].uuid", is("9d2fbdfb-e835-4f4e-aca1-071a66d9845c")))
                .andExpect(jsonPath("$.[0].name", is("Alberta Beardtongue")))
                .andExpect(jsonPath("$.[0].quantity", is(8661)))
                .andExpect(jsonPath("$.[0].unitPrice", is(7798.75)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9680.39)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLtQuantityFilter_thenOkAndShouldReturnArrayFilteredByLtQuantity() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?lt.quantity=610")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(3)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "3"))
                .andExpect(jsonPath("$.[0].uuid", is("3b95695c-ca0a-491b-9fb9-606b91c125bc")))
                .andExpect(jsonPath("$.[0].name", is("Compact Phacelia")))
                .andExpect(jsonPath("$.[0].quantity", is(571)))
                .andExpect(jsonPath("$.[0].unitPrice", is(3632.91)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9608.18)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLeQuantityFilter_thenOkAndShouldReturnArrayFilteredByLeQuantity() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?le.quantity=4198")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "34"))
                .andExpect(jsonPath("$.[0].uuid", is("9e4f7f11-d286-4dcc-9324-a0c824ec564f")))
                .andExpect(jsonPath("$.[0].name", is("Bachman's Jelly Lichen")))
                .andExpect(jsonPath("$.[0].quantity", is(3024)))
                .andExpect(jsonPath("$.[0].unitPrice", is(841.24)))
                .andExpect(jsonPath("$.[0].packageVolume", is(1894.59)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithEqUnitPriceFilter_thenOkAndShouldReturnArrayFilteredByUnitPriceEquality() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?eq.unitPrice=4418.98")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "1"))
                .andExpect(jsonPath("$.[0].uuid", is("c455da23-fe31-43a5-b7fe-5420d1df4fa0")))
                .andExpect(jsonPath("$.[0].name", is("Carolina Yelloweyed Grass")))
                .andExpect(jsonPath("$.[0].quantity", is(7413)))
                .andExpect(jsonPath("$.[0].unitPrice", is(4418.98)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9158.1)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithGtUnitPriceFilter_thenOkAndShouldReturnArrayFilteredByGtUnitPrice() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?gt.unitPrice=9804.65")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "1"))
                .andExpect(jsonPath("$.[0].uuid", is("023407d9-f2f2-4dc5-ad2f-2ac412148d55")))
                .andExpect(jsonPath("$.[0].name", is("Gymnanthes")))
                .andExpect(jsonPath("$.[0].quantity", is(3784)))
                .andExpect(jsonPath("$.[0].unitPrice", is(9921.78)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9406.33)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithGeUnitPriceFilter_thenOkAndShouldReturnArrayFilteredByGeUnitPrice() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?ge.unitPrice=8859.6")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(7)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "7"))
                .andExpect(jsonPath("$.[0].uuid", is("023407d9-f2f2-4dc5-ad2f-2ac412148d55")))
                .andExpect(jsonPath("$.[0].name", is("Gymnanthes")))
                .andExpect(jsonPath("$.[0].quantity", is(3784)))
                .andExpect(jsonPath("$.[0].unitPrice", is(9921.78)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9406.33)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLtUnitPriceFilter_thenOkAndShouldReturnArrayFilteredByLtUnitPrice() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?lt.unitPrice=4579.84")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "50"))
                .andExpect(jsonPath("$.[0].uuid", is("9e4f7f11-d286-4dcc-9324-a0c824ec564f")))
                .andExpect(jsonPath("$.[0].name", is("Bachman's Jelly Lichen")))
                .andExpect(jsonPath("$.[0].quantity", is(3024)))
                .andExpect(jsonPath("$.[0].unitPrice", is(841.24)))
                .andExpect(jsonPath("$.[0].packageVolume", is(1894.59)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLeUnitPriceFilter_thenOkAndShouldReturnArrayFilteredByLeUnitPrice() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?le.unitPrice=1662.67")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "16"))
                .andExpect(jsonPath("$.[0].uuid", is("9e4f7f11-d286-4dcc-9324-a0c824ec564f")))
                .andExpect(jsonPath("$.[0].name", is("Bachman's Jelly Lichen")))
                .andExpect(jsonPath("$.[0].quantity", is(3024)))
                .andExpect(jsonPath("$.[0].unitPrice", is(841.24)))
                .andExpect(jsonPath("$.[0].packageVolume", is(1894.59)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithEqPackageVolumeFilter_thenOkAndShouldReturnArrayFilteredByPackageVolumeEquality() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?eq.packageVolume=9158.1")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "1"))
                .andExpect(jsonPath("$.[0].uuid", is("c455da23-fe31-43a5-b7fe-5420d1df4fa0")))
                .andExpect(jsonPath("$.[0].name", is("Carolina Yelloweyed Grass")))
                .andExpect(jsonPath("$.[0].quantity", is(7413)))
                .andExpect(jsonPath("$.[0].unitPrice", is(4418.98)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9158.1)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithGtPackageVolumeFilter_thenOkAndShouldReturnArrayFilteredByGtPackageVolume() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?gt.packageVolume=9804.65")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "2"))
                .andExpect(jsonPath("$.[0].uuid", is("e08b222f-9402-46fa-9063-5fd78ac3dff9")))
                .andExpect(jsonPath("$.[0].name", is("Hoja Menuda")))
                .andExpect(jsonPath("$.[0].quantity", is(6811)))
                .andExpect(jsonPath("$.[0].unitPrice", is(5995.28)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9949.17)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithGePackageVolumeFilter_thenOkAndShouldReturnArrayFilteredByGePackageVolume() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?ge.packageVolume=8859.6")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "13"))
                .andExpect(jsonPath("$.[0].uuid", is("9d2fbdfb-e835-4f4e-aca1-071a66d9845c")))
                .andExpect(jsonPath("$.[0].name", is("Alberta Beardtongue")))
                .andExpect(jsonPath("$.[0].quantity", is(8661)))
                .andExpect(jsonPath("$.[0].unitPrice", is(7798.75)))
                .andExpect(jsonPath("$.[0].packageVolume", is(9680.39)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLtPackageVolumeFilter_thenOkAndShouldReturnArrayFilteredByLtPackageVolume() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?lt.packageVolume=4579.84")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "51"))
                .andExpect(jsonPath("$.[0].uuid", is("cd2b22e0-31e1-4cd9-af70-93d0e67e66d7")))
                .andExpect(jsonPath("$.[0].name", is("Apache Beardtongue")))
                .andExpect(jsonPath("$.[0].quantity", is(6152)))
                .andExpect(jsonPath("$.[0].unitPrice", is(8661.12)))
                .andExpect(jsonPath("$.[0].packageVolume", is(1990.37)));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = IMPORT_INVENTORY_ITEMS_SCRIPT),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = DELETE_IMPORT_INVENTORY_STATEMENT)
    })
    void givenSeedDataFromImportSql_whenGetAllWithLePackageVolumeFilter_thenOkAndShouldReturnArrayFilteredByLePackageVolume() throws Exception {
        //given
        //we have the import-entity.sql script file loaded

        //when
        this.mockMvc.perform(
                        get(apiUrl + "?le.packageVolume=1662.67")
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "16"))
                .andExpect(jsonPath("$.[0].uuid", is("5e442c9f-d0f4-4728-8958-005f5b2f22be")))
                .andExpect(jsonPath("$.[0].name", is("Branching Phacelia")))
                .andExpect(jsonPath("$.[0].quantity", is(4927)))
                .andExpect(jsonPath("$.[0].unitPrice", is(6677.62)))
                .andExpect(jsonPath("$.[0].packageVolume", is(392.44)));

    }
}
