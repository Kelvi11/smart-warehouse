package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.Order;
import com.kelvin.smartwarehouse.model.Truck;
import com.kelvin.smartwarehouse.model.enums.OrderStatus;
import com.kelvin.smartwarehouse.utils.CsvUtils;
import com.kelvin.smartwarehouse.utils.XlsxUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDERS_URL;
import static com.kelvin.smartwarehouse.management.AppConstants.TRUCK_URL;

@RestController
@RequestMapping(TRUCK_URL)
public class TruckApi extends BaseApi<Truck> {

    public TruckApi() {
        super(Truck.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "licensePlate asc";
    }

    @Override
    protected List<Predicate> getFilters(CriteriaBuilder criteriaBuilder, Root<Truck> root){
        List<Predicate> predicates = new ArrayList<>();

        if (nn("obj.chassisNumber")) {
            Path<String> orderUuid = root.get("chassisNumber");
            predicates.add(criteriaBuilder.equal(orderUuid, get("obj.chassisNumber")));
        }

        if (nn("obj.licensePlate")) {
            Path<String> orderUuid = root.get("licensePlate");
            predicates.add(criteriaBuilder.equal(orderUuid, get("obj.licensePlate")));
        }

        buildDoubleFieldFilters("packageVolume", criteriaBuilder, root, predicates);

        return predicates;
    }

    @Override
    protected void prePersist(Truck truck) throws Exception {
        if (truck.getChassisNumber() == null || truck.getChassisNumber().isEmpty()){
            throw new InvalidParameterException("Truck chassis number is required!");
        }
        if (truck.getLicensePlate() == null || truck.getLicensePlate().isEmpty()){
            throw new InvalidParameterException("Truck license plate is required!");
        }
        if (truck.getContainerVolume() < 0){
            throw new InvalidParameterException("Truck container volume can't be a negative value!");
        }
        //verify that chassis number or license plate is unique
    }

//    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
//    @Transactional
//    public ResponseEntity exportOrders(@RequestParam(defaultValue = "csv") String type) throws Exception {
//
//        File file;
//        List<Order> list = getAll();
//        if (type.equals("csv")) {
//            file = Files.createTempFile("orders", ".csv").toFile();
//            CsvUtils.writeCsv(new FileWriter(file), list);
//        }
//        else if(type.equals("xlsx")){
//            file = Files.createTempFile("orders", ".xlsx").toFile();
//            XlsxUtils.writeXlsx(file, list);
//        }
//        else {
//            String message = String.format("%s type is not supported for the orders export.", type);
//            throw new InvalidParameterException(message);
//        }
//
//        FileInputStream fileInputStream = new FileInputStream(file);
//
//        byte[] content = fileInputStream.readAllBytes();
//
//        return ResponseEntity.ok(content);
//    }
//
//    private List<Order> getAll() {
//        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
//
//        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(getEntityClass());
//
//        Root<Order> root = criteriaQuery.from(getEntityClass());
//        criteriaQuery.select(root);
//
//        TypedQuery<Order> search = getEntityManager().createQuery(criteriaQuery);
//
//        return search.getResultList();
//    }
}

