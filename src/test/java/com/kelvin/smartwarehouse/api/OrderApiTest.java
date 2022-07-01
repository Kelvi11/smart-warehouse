package com.kelvin.smartwarehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelvin.smartwarehouse.model.OrderStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void shouldReturnOrderList() {
    }

    @Test
    @Order(2)
    void shouldPersistOrder() throws Exception {
        com.kelvin.smartwarehouse.model.Order order = buildOrder();

        String jsonBody = objectMapper.writeValueAsString(order);

        this.mockMvc.perform(
                        post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))

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
    void shouldFetchOrderById() {
    }

    @Test
    @Order(2)
    void shouldUpdateOrder() {
    }

    @Test
    @Order(2)
    void shouldDeleteOrder() {
    }
}
