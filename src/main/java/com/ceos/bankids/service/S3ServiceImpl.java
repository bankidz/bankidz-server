package com.ceos.bankids.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.PreSignedDTO;
import com.ceos.bankids.exception.BadRequestException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //preSignedUrl 받아오기 API
    @Override
    public PreSignedDTO readPreSignedUrl(User user) {

        Date date = new Date();
        long time = date.getTime();
        time += 1000 * 60 * 30;
        date.setTime(time);
        Long userId = user.getId();
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String fileName = userId + "-" + uuidString + ".png";
        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                bucket, fileName).withMethod(HttpMethod.PUT)
                .withExpiration(date);
            generatePresignedUrlRequest.setContentType("image/png");
            generatePresignedUrlRequest.addRequestParameter(Headers.S3_CANNED_ACL,
                CannedAccessControlList.PublicRead.toString());
            URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return new PreSignedDTO(url, fileName);
        } catch (NullPointerException e) {
            throw new BadRequestException(ErrorCode.PRESIGNEDURI_NPE.getErrorCode());
        }
    }
}
