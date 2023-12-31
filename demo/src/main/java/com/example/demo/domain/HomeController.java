package com.example.demo.domain;

import com.example.demo.core.security.JwtTokenProvider;
import com.example.demo.member.Member;
import com.example.demo.member.MemberService;
import com.example.demo.product.ProductResponse;
import com.example.demo.product.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class HomeController {


    private final ProductService productService;
    private final MemberService memberService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {


        // 쿠키에서 토큰을 가져옵니다.
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    // 토큰에서 이메일 정보를 가져옵니다.
                    String email = JwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, "")).getSubject();
                    Optional<Member> member = memberService.findByMemberEmail(email);
                    if (member.isPresent()) {
                        model.addAttribute("memberEmail", member.get().getEmail());
                    }
                    break;
                }
            }
        }

        return "index";
    }

    @GetMapping("/product/create")
    public String create(){
        return "create";
    }

    @GetMapping("/products/paging")
    public String productList(@PageableDefault(page = 1)Pageable pageable, Model model) {

        Page<ProductResponse.PagingDto> products = productService.paging(pageable);

        int blockLimit = 3;
        int startPage = (int)(Math.ceil((double)pageable.getPageNumber() / blockLimit) - 1) * blockLimit + 1;
        int endPage = ((startPage + blockLimit - 1) < products.getTotalPages()) ? (startPage + blockLimit - 1) : products.getTotalPages();

        model.addAttribute("productList", products);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage",endPage);

        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductResponse.FindByIdDTO productDTO = productService.findByProductId(id);
        model.addAttribute("product", productDTO);
        return "detail";
    }

    @GetMapping("/loginPage")
    public String login() {
        return "login"; // 로그인 페이지로 이동하는 뷰 이름
    }

    @GetMapping("/joinPage")
    public String join() {
        return "join"; // 회원가입 페이지로 이동하는 뷰 이름
    }



}
