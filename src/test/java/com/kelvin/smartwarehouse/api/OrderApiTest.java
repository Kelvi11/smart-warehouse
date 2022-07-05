package com.kelvin.smartwarehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelvin.smartwarehouse.exception.EntityWithIdNotFoundException;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.management.AppConstants;
import com.kelvin.smartwarehouse.model.OrderStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDERS_URL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderApiTest {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    @Autowired
    public OrderApiTest(ObjectMapper mapper, MockMvc mockMvc) {
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
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/orders_schema.sql", "/import_orders.sql"}),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = "delete from orders")
    })
    void givenSeedDataFromImportOrdersSql_whenGetAll_thenOkAndShouldReturnOrdersArray() throws Exception {
        //given
        //we have the data.sql script file loaded

        //when
        this.mockMvc.perform(
                get(ORDERS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(10)))
                .andExpect(header().string("startRow", "0"))
                .andExpect(header().string("pageSize", "10"))
                .andExpect(header().string("listSize", "25"))
                .andExpect(jsonPath("$.[0].uuid", is("b2e9f0ed-1364-45e6-9d3a-5cc5456e75f9")))
                .andExpect(jsonPath("$.[0].submittedDate", is("2022-07-01")))
                .andExpect(jsonPath("$.[0].deadlineDate", is("2022-07-10")))
                .andExpect(jsonPath("$.[0].status", is("CREATED")));

    }

    @Test
    @Order(2)
    void givenEmptyTable_whenGetAll_thenOkAndShouldReturnEmptyArray() throws Exception {
        //given
        //we have the table empty

        //when
        this.mockMvc.perform(
                        get(ORDERS_URL)
                                .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));

    }


    @Test
    @Order(2)
    void givenOrder_whenPostOrder_thenShouldPersistOrder() throws Exception {
        //given
        com.kelvin.smartwarehouse.model.Order order = buildOrder();

        String jsonBody = objectMapper.writeValueAsString(order);

        //when
        this.mockMvc.perform(
                        post(ORDERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(notNullValue())))
                .andExpect(jsonPath("$.submittedDate", is(order.getSubmittedDate().toString())))
                .andExpect(jsonPath("$.deadlineDate", is(order.getDeadlineDate().toString())))
                .andExpect(jsonPath("$.status", is(order.getStatus().toString())));

    }

    private com.kelvin.smartwarehouse.model.Order buildOrder() {
        com.kelvin.smartwarehouse.model.Order order = new com.kelvin.smartwarehouse.model.Order();
        
        order.setSubmittedDate(LocalDate.now());
        order.setDeadlineDate(LocalDate.now().plusDays(2));
        order.setStatus(OrderStatus.CREATED);
        
        return order;
    }

    @Test
    @Order(2)
    void givenOrderWithoutDeadlinedDate_whenPostOrder_thenShouldReturn4xxClientError() throws Exception {
        //given
        com.kelvin.smartwarehouse.model.Order order = buildOrder();
        order.setDeadlineDate(null);

        String jsonBody = objectMapper.writeValueAsString(order);

        //when
        this.mockMvc.perform(
                        post(ORDERS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Order deadline date is required!")));

    }

    @Test
    @Order(2)
    void givenOrderWithDeadlinedDateInThePast_whenPostOrder_thenShouldReturn4xxClientError() throws Exception {
        //given
        com.kelvin.smartwarehouse.model.Order order = buildOrder();
        order.setDeadlineDate(LocalDate.now().minusDays(2));

        String jsonBody = objectMapper.writeValueAsString(order);

        //when
        this.mockMvc.perform(
                        post(ORDERS_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                //then
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidParameterException))
                .andExpect(jsonPath("$.message", is("Order deadline date should be in the future!")));

    }

    @Test
    @Order(2)
    @Sql({"/orders_schema.sql", "/import_orders.sql"})
    void givenSeedDataFromImportOrdersSqlAndId_whenGetById_thenOkAndShouldReturnOrderWithGivenId() throws Exception {

        //given
        //the data imported from import_orders.sql
        String id = "9fe2e517-c135-4f3e-a1c2-705e5b59a4f7";

        //when
        this.mockMvc.perform(
                        get(ORDERS_URL+ "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("9fe2e517-c135-4f3e-a1c2-705e5b59a4f7")))
                .andExpect(jsonPath("$.submittedDate", is("2022-06-15")))
                .andExpect(jsonPath("$.deadlineDate", is("2022-06-30")))
                .andExpect(jsonPath("$.status", is("FULFILLED")));
    }

    @Test
    @Order(2)
    void givenEmptyOrdersListAndId_whenGetById_thenShouldReturn4xxClientError() throws Exception {

        //given
        //the data imported from import_orders.sql
        String id = "IdNotPresentInDb";

        //when
        this.mockMvc.perform(
                        get(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isNoContent())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityWithIdNotFoundException))
                .andExpect(jsonPath("$.message", is("Order with id [IdNotPresentInDb] doesn't exist in database!")));
    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/orders_schema.sql", "/import_orders.sql"}),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = "delete from orders")
    })
    void givenSeedDataFromImportOrdersSqlAndId_whenUpdate_thenOkStatusAndShouldUpdateOrder() throws Exception {

        //given
        String id = "051191d4-4eba-48ca-9a8c-19076eb7f669";
        String requestBody = "{\n" +
                "    \"uuid\" : \"051191d4-4eba-48ca-9a8c-19076eb7f669\",\n" +
                "    \"submittedDate\" : \"2022-05-01\",\n" +
                "    \"deadlineDate\" : \"2022-05-15\",\n" +
                "    \"status\" : \"FULFILLED\"\n" +
                "}";

        //when
        this.mockMvc.perform(
                        put(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("051191d4-4eba-48ca-9a8c-19076eb7f669")))
                .andExpect(jsonPath("$.submittedDate", is("2022-05-01")))
                .andExpect(jsonPath("$.deadlineDate", is("2022-05-15")))
                .andExpect(jsonPath("$.status", is("FULFILLED")));

    }

    @Test
    @Order(2)
    void givenEmptyOrdersListAndIdAndOrder_whenUpdate_thenOkStatusAndShouldCreateNewOrder() throws Exception {

        //given
        String id = "051191d4-4eba-48ca-9a8c-19076eb7f669";
        String requestBody = "{\n" +
                "    \"uuid\" : \"051191d4-4eba-48ca-9a8c-19076eb7f669\",\n" +
                "    \"submittedDate\" : \"2022-05-01\",\n" +
                "    \"deadlineDate\" : \"2022-05-15\",\n" +
                "    \"status\" : \"FULFILLED\"\n" +
                "}";

        //when
        this.mockMvc.perform(
                        put(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is("051191d4-4eba-48ca-9a8c-19076eb7f669")))
                .andExpect(jsonPath("$.submittedDate", is("2022-05-01")))
                .andExpect(jsonPath("$.deadlineDate", is("2022-05-15")))
                .andExpect(jsonPath("$.status", is("FULFILLED")));

    }

    @Test
    @Order(2)
    @SqlGroup({
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/orders_schema.sql", "/import_orders.sql"}),
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = "delete from orders")
    })
    void givenSeedDataFromImportOrdersSqlAndId_whenDelete_thenNotFoundStatusAndShouldDeleteOrder() throws Exception {

        //given
        String id = "051191d4-4eba-48ca-9a8c-19076eb7f669";

        //when
        this.mockMvc.perform(
                        delete(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isNoContent());

        this.mockMvc.perform(
                        get(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityWithIdNotFoundException))
                .andExpect(jsonPath("$.message", is("Order with id [051191d4-4eba-48ca-9a8c-19076eb7f669] doesn't exist in database!")));

    }

    @Test
    @Order(2)
    void givenEmptyOrdersListAndId_whenDelete_thenNotFoundStatus() throws Exception {

        //given
        String id = "IdNotPresentInDb";

        //when
        this.mockMvc.perform(
                        delete(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNoContent());

        this.mockMvc.perform(
                        get(ORDERS_URL + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityWithIdNotFoundException))
                .andExpect(jsonPath("$.message", is("Order with id [IdNotPresentInDb] doesn't exist in database!")));

    }
}
