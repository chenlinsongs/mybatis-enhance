package cus.mybatis.enhance.core.page;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;

public interface Pageable<T> {

    @JsonIgnore
    @XmlTransient
    public Page<T> getPage();


    public Page<T> setPage(Page<T> page);
}
