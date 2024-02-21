package group2.webapp.FinalProject4.controllers;

import group2.webapp.FinalProject4.models.Bill;
import group2.webapp.FinalProject4.models.BillDetail;
import group2.webapp.FinalProject4.models.Product;
import group2.webapp.FinalProject4.models.User;
import group2.webapp.FinalProject4.models.keys.BillDetailKey;
import group2.webapp.FinalProject4.services.BillDetailService;
import group2.webapp.FinalProject4.services.BillService;
import group2.webapp.FinalProject4.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class CartController {

    @Autowired
    ProductService productService;

    @Autowired
    BillService billService;

    @Autowired
    BillDetailService billDetailService;
    @RequestMapping(value = "/add-to-cart/{product}/{quantity}")
    public String AddToCart(@PathVariable Integer product,
                            @PathVariable Integer quantity,
                            HttpServletRequest rq) {
        HttpSession session = rq.getSession();
        Product productModel = productService.getProductById(product);
        User user = (User) session.getAttribute("account");
        System.out.println("Product: " + product + " - Quantity : "+ quantity);

        if (user == null) {
            return "Vui lòng đăng nhập!!!";
        } else {
            Optional<Bill> billCheck = billService.findByUserAndStatus(user,-1);
            if(billCheck.isPresent()){ // Đã có giỏ hàng
                Bill bill = billCheck.get();
                BillDetail billDetail = null;
                Optional<BillDetail> billDetailCheck = billDetailService.findBillDetailsByProductIdAndBillId(productModel,bill);
                if(billDetailCheck.isPresent()){
                    billDetail = billDetailCheck.get();
                    Integer newQuantity = billDetail.getQuantity() + quantity;
                    billDetail.setQuantity(newQuantity);
                }else{
                    billDetail = new BillDetail();
                    billDetail.setBillId(bill);
                    billDetail.setProductId(productModel);
                    billDetail.setQuantity(quantity);
                }
                List<BillDetail> list1 = billDetailService.findAllByBillId(bill);
                double total = 0;
                for(BillDetail b: list1){
                    total += b.getQuantity() * b.getProductId().getPrice();
                }
                bill.setTotal(total + (billDetail.getProductId().getPrice()*billDetail.getQuantity()));
                billService.saveBill(bill);
                billDetailService.saveBillDetail(billDetail);

                return "Thêm thành công";
            } else { // Nếu chưa có giỏ hàng mới
                Bill bill = new Bill();
                bill.setStatus(-1);
                Date date = Date.valueOf(LocalDate.now());
                bill.setDate(date);
                bill.setUser(user);

                billService.saveBill(bill);
                BillDetail billDetail = new BillDetail();


                billDetail.setBillId(bill);
                billDetail.setProductId(productModel);
                billDetail.setQuantity(quantity);
                List<BillDetail> list2 = billDetailService.findAllByBillId(bill);
                double total = 0;
                for(BillDetail b: list2){
                    total += b.getQuantity() * b.getProductId().getPrice();
                }
                bill.setTotal(total + (billDetail.getProductId().getPrice()*billDetail.getQuantity()));

                billService.saveBill(bill);
                billDetailService.saveBillDetail(billDetail);
                return "Thêm thành công";
            }
        }


    }

    @RequestMapping(value = "/remove-cart/{productID}/{subtotal}")
    public String RemoveCartItem(@PathVariable Integer productID,
                                 @PathVariable double subtotal,
                                 HttpServletRequest rq){

        HttpSession session = rq.getSession();
        User user = (User) session.getAttribute("account");
        if (user == null) {
            return "Vui lòng đăng nhập!!!";
        }else{
            Optional<Bill> billCheck = billService.findByUserAndStatus(user, -1);
            if(billCheck.isPresent()){
                Bill bill = billCheck.get();
                BillDetailKey billDetailKey = new BillDetailKey();
                billDetailKey.setBillId(bill.getId());
                billDetailKey.setProductId(productID);
                billDetailService.removeBillDetail(billDetailKey);

                double newTotal = bill.getTotal() - subtotal ;
                bill.setTotal(newTotal);
                billService.saveBill(bill);
                return ""+user.getId();
            }else {
                return "Error";
            }
        }

    }


}
