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

import static com.kelvin.smartwarehouse.api.InventoryItemApiTest.createSchemaScript;
import static com.kelvin.smartwarehouse.management.AppConstants.INVENTORY_ITEMS_URL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = createSchemaScript)
public class InventoryItemApiTest {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    static final String createSchemaScript = "/inventory_item/inventory_items_schema.sql";
    static final String importRecordsScript = "/inventory_item/import_inventory_items.sql";
    static final String deleteStatement = "delete from inventory_items ";
    
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
                .andExpect(jsonPath("$.message", is("Inventory item with id [IdNotPresentInDb] doesn't exist in database!")));
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
                .andExpect(jsonPath("$.message", is("Inventory item with id [8af18e2f-95b5-4569-abde-ebc39746fc99] doesn't exist in database!")));

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
                .andExpect(jsonPath("$.message", is("Inventory item with id [IdNotPresentInDb] doesn't exist in database!")));

    }
}
