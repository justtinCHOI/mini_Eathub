package com.eathub.entity;

import com.eathub.entity.ENUM.MENU_TYPE;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@Data
@Builder
public class Menu {
    private Long menu_seq;
    private Long restaurant_seq;
    private String menu_name;
    private String menu_info;
    private String menu_image;
    private String menu_image_path;
    private String  menu_price;
    private MENU_TYPE menu_type;
}
