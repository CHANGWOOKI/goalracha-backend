package com.goalracha.controller;


import com.goalracha.dto.GroundDTO;
import com.goalracha.dto.PageRequestDTO;
import com.goalracha.dto.PageResponseDTO;
import com.goalracha.service.GroundService;
import com.goalracha.util.CustomFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/goalracha/ground")
public class GroundController {
    private final GroundService service;
    private final CustomFileUtil fileUtil;

    @GetMapping("/read/{gno}")
    public GroundDTO get(@PathVariable(name = "gno") Long gno) {
        return service.get(gno);
    }

    @GetMapping("/")
    public PageResponseDTO<GroundDTO> listWithImage(PageRequestDTO pageRequestDTO) {
        log.info(pageRequestDTO);

        return service.listWithImage(pageRequestDTO);
    }

    @PostMapping("/register")
    public Map<String, Long> register( GroundDTO groundDTO) {
        log.info("register: " + groundDTO);
        // 파일 저장
        List<MultipartFile> files = groundDTO.getFiles();
        List<String> uploadFileNames = fileUtil.saveFiles(files);
        groundDTO.setUploadFileNames(uploadFileNames);
        log.info(uploadFileNames);
        // 서비스
        Long gNo = service.register(groundDTO);
        return Map.of("result", gNo);
    }

    @PutMapping("/modify/{gno}")
    public Map<String, String> modify(@PathVariable(name = "gno") Long gno, @RequestBody GroundDTO groundDTO) {

        log.info("Modify: " + groundDTO);

        groundDTO.setGNo(gno);
        GroundDTO oldGroundDTO = service.get(gno);

        List<String> oldFileNames = oldGroundDTO.getUploadFileNames();
        List<MultipartFile> files = groundDTO.getFiles();
        List<String> currentUploadFileNames = fileUtil.saveFiles(files);
        List<String> uploadedFileNames = groundDTO.getUploadFileNames();

        if (currentUploadFileNames != null && currentUploadFileNames.size() > 0) {
            uploadedFileNames.addAll(currentUploadFileNames);
        }
        service.modify(groundDTO);


        if (oldFileNames != null && oldFileNames.size() > 0) {
            List<String> removeFiles = oldFileNames.stream().filter(fileName ->
                    uploadedFileNames.indexOf(fileName) == -1).collect(Collectors.toList());

            fileUtil.deleteFiles(removeFiles);
        }

        return Map.of("RESULT", "SUCCESS");
    }
    @DeleteMapping("/delete/{gno}")
    public Map<String, String> delete(@PathVariable(name="gno") Long gno) {

        log.info("Remove: " + gno);

        List<String> oldFileNames = service.get(gno).getUploadFileNames();

        service.delete(gno);
        fileUtil.deleteFiles(oldFileNames);

        return Map.of("RESULT", "SUCCESS");
    }

    @PutMapping("/changeState/{gno}")
    public Map<String, String> changeState(@PathVariable(name = "gno") Long gno, @RequestBody Map<String, Long> stateMap) {
        Long newState = stateMap.get("newState");
        service.changeState(gno, newState);
        return Map.of("RESULT", "SUCCESS", "gNo", gno.toString(), "newState", newState.toString());
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName) {
        return fileUtil.getFile(fileName);
    }
}

