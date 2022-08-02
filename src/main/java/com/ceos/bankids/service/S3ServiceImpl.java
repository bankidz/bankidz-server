package com.ceos.bankids.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.PreSignedDTO;
import com.ceos.bankids.exception.BadRequestException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
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

        ZonedDateTime expiredDate = ZonedDateTime.now().plusMinutes(2);
        Long userId = user.getId();
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String fileName = userId + "-" + uuidString;
        URI uri;
        try {
            uri = this.amazonS3Client.generatePresignedUrl(bucket, fileName,
                Date.from(expiredDate.toInstant()), HttpMethod.PUT).toURI();
        } catch (URISyntaxException e) {
            throw new BadRequestException("URI 생성 중 오류 발생");
        } catch (NullPointerException e) {
            throw new BadRequestException("널포인트에러");
        }
        String preSignedUrl = uri.toString();
        return new PreSignedDTO(preSignedUrl, fileName);
    }
}
