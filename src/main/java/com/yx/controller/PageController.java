package com.yx.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器
 */
@Slf4j
@Controller
public class PageController {

    /**
     * 根路径跳转到登录页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    /**
     * 登录页 (直接访问 /login 会使用templates/login.html)
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 管理员仪表板及子页面（支持直接访问/刷新，SPA 由前端根据 path 加载对应 fragment）
     */
    @GetMapping({"/admin/dashboard", "/admin/readers", "/admin/books", "/admin/lends", "/admin/announcements"})
    public String adminDashboard(Model model) {
        model.addAttribute("title", "管理员仪表板");
        return "admin/dashboard";
    }

    /**
     * 管理端片段：仪表盘内容
     */
    @GetMapping("/admin/fragment/dashboard")
    public String adminDashboardFragment(Model model) {
        model.addAttribute("title", "管理员仪表盘");
        return "admin/fragment_dashboard";
    }

    /**
     * 管理端片段：图书管理占位
     */
    @GetMapping("/admin/fragment/books")
    public String adminBooksFragment(Model model) {
        model.addAttribute("title", "图书管理");
        return "admin/fragment_books";
    }

    /**
     * 管理端片段：读者管理占位
     */
    @GetMapping("/admin/fragment/readers")
    public String adminReadersFragment(Model model) {
        model.addAttribute("title", "读者管理");
        return "admin/fragment_readers";
    }

    /**
     * 管理端片段：借阅记录占位
     */
    @GetMapping("/admin/fragment/lends")
    public String adminLendsFragment(Model model) {
        model.addAttribute("title", "借阅记录");
        return "admin/fragment_lends";
    }

    @GetMapping("/admin/fragment/announcements")
    public String adminAnnouncementsFragment(Model model) {
        model.addAttribute("title", "发布公告");
        return "admin/fragment_announcements";
    }

    /**
     * 读者仪表板
     */
    @GetMapping("/reader/dashboard")
    public String readerDashboard(Model model) {
        model.addAttribute("title", "读者仪表板");
        return "reader/dashboard";
    }

    /**
     * 读者端片段：仪表盘内容
     */
    @GetMapping("/reader/fragment/dashboard")
    public String readerDashboardFragment(Model model) {
        model.addAttribute("title", "我的概览");
        return "reader/fragment_dashboard";
    }

    /**
     * 读者端片段：图书查询与借阅
     */
    @GetMapping("/reader/fragment/books")
    public String readerBooksFragment(Model model) {
        model.addAttribute("title", "图书查询");
        return "reader/fragment_books";
    }

    /**
     * 读者端片段：借阅历史
     */
    @GetMapping("/reader/fragment/lends")
    public String readerLendsFragment(Model model) {
        model.addAttribute("title", "我的借阅");
        return "reader/fragment_lends";
    }

    /**
     * 读者端片段：个人信息
     */
    @GetMapping("/reader/fragment/profile")
    public String readerProfileFragment(Model model) {
        model.addAttribute("title", "个人信息");
        return "reader/fragment_profile";
    }

    /**
     * 读者端片段：消息中心
     */
    @GetMapping("/reader/fragment/messages")
    public String readerMessagesFragment(Model model) {
        model.addAttribute("title", "消息中心");
        return "reader/fragment_messages";
    }

    /**
     * 读者端通用路由跳转 (支持 SPA 页面刷新)
     */
    @GetMapping({"/reader/books", "/reader/lends", "/reader/profile", "/reader/messages"})
    public String readerCommonPages() {
        return "reader/dashboard";
    }
}
