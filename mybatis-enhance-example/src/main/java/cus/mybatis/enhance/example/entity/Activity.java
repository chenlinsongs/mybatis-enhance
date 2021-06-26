package cus.mybatis.enhance.example.entity;

import cus.mybatis.enhance.core.annotation.Primary;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

//@Table(name = "activity")
public class Activity implements Serializable {

//    @Primary(name = "id")
    private Integer id;

//    @Column(name = "name")
    private String name;

    private String teacherInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherInfo() {
        return teacherInfo;
    }

    public void setTeacherInfo(String teacherInfo) {
        this.teacherInfo = teacherInfo;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", teacherInfo='" + teacherInfo + '\'' +
                '}';
    }
}
