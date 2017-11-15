namespace java com.paner.thrift.study.dto

//Person类
struct Person {
  1: i16 age = 0,
  2: string name,
  3: bool sex,
  4: double salary,
  5: byte childrenCount
}

//查询参数
struct QueryParameter{
 1: i16 ageStart,
 2: i16 ageEnd
}