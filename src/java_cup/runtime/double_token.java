package java_cup.runtime;






public class double_token
  extends token
{
  public double double_val;
  




  public double_token(int term_num, double v)
  {
    super(term_num);
    
    double_val = v;
  }
  

  public double_token(int term_num)
  {
    this(term_num, 0.0D);
  }
}
