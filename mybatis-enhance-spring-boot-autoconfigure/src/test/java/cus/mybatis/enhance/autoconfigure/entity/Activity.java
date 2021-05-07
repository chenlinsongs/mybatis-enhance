package cus.mybatis.enhance.autoconfigure.entity;

import cus.mybatis.enhance.core.annotation.Primary;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "activity")
public class Activity implements Serializable {

    @Primary(name = "id")
    Integer id;

    @Column(name = "name")
    String name;

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

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
