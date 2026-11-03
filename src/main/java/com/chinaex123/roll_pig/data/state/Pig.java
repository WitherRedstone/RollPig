package com.chinaex123.roll_pig.data.state;

/**
 * 一只猪猪的静态资料，对应 data/roll_pig/pigsty 目录里的一份 json。
 *
 * @param id          猪猪的唯一标识，同时也是图片文件名（不含 .png）
 * @param name        显示名称，例如「普通小猪」
 * @param description 一句话短描述，例如「普通温顺」
 * @param analysis    多行概述，显示在详情面板里
 */
public record Pig(String id, String name, String description, String analysis) { }
