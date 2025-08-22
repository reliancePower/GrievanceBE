package com.reliance.grievance.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HrDirectoryInfo {
    private  String mobile;
    private  String prNo;
    private  String location;
    private  String dept;
    private  boolean allowed;

    public HrDirectoryInfo(String mobile, String prNo, String location, String dept, boolean allowed) {
        this.mobile = mobile;
        this.prNo = prNo;
        this.location = location;
        this.dept = dept;
        this.allowed = allowed;
    }
}
