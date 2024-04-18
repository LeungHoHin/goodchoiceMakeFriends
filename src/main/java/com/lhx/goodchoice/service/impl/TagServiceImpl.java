package com.lhx.goodchoice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhx.goodchoice.pojo.Tag;
import com.lhx.goodchoice.service.TagService;
import com.lhx.goodchoice.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 梁浩轩
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-04-16 16:32:57
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




