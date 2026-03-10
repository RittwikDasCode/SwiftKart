package com.ecommerce.project.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

	@Override
	public String uploadImage(String path, MultipartFile file) throws IOException {
		// Filename of original file
		String originamFileName = file.getOriginalFilename();
		
		//Generate an unique filename
		String randomId = UUID.randomUUID().toString();
		
		//map.jpg -->1234 -->1234.jpg
		String fileName = randomId.concat(originamFileName.substring(originamFileName.lastIndexOf('.')));
		String filePath = path + File.separator+ fileName;
		
		//Check if path exist and create
		File folder = new File(path);
		if(!folder.exists())
			folder.mkdir();
		
		//Upload to the server
		Files.copy(file.getInputStream(), Paths.get(filePath));
		return fileName;
	}
}
