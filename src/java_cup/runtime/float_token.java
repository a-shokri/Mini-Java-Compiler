package java_cup.runtime;






public class float_token
  extends token
{
  public float float_val;
  




  public float_token(int term_num, float v)
  {
    super(term_num);
    
    float_val = v;
  }
  

  public float_token(int term_num)
  {
    this(term_num, 0.0F);
  }
}
