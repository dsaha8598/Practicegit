package com.udajahaja;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/")

public class TestController {

	@GetMapping("testme")
	public @ResponseBody String test() {
		return "success";
	}
}
