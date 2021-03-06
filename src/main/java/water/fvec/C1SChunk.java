package water.fvec;

import water.*;
import water.parser.DParseTask;

// The scale/bias function, where data is in SIGNED bytes before scaling
public class C1SChunk extends Chunk {
  static private final long _NA = 0xFF;
  static final int OFF=8+4;
  public double _scale;
  int _bias;
  C1SChunk( byte[] bs, int bias, double scale ) { _mem=bs; _start = -1; _len = _mem.length-OFF;
    _bias = bias; _scale = scale;
    UDP.set8d(_mem,0,scale);
    UDP.set4 (_mem,8,bias );
  }
  @Override protected final long at8_impl( int i ) {
    long res = 0xFF&_mem[i+OFF];
    return res == _NA?_vec._iNA:(long)((res+_bias)*_scale);
  }
  @Override protected final double atd_impl( int i ) {
    long res = 0xFF&_mem[i+OFF];
    return (res == _NA)?_vec._fNA:(res+_bias)*_scale;
  }
  @Override boolean set8_impl(int i, long l) { 
    long res = (long)(l/_scale)-_bias; // Compressed value
    double d = (res+_bias)*_scale;     // Reverse it
    if( (long)d != l ) return false;   // Does not reverse cleanly?
    if( !(0 <= res && res < 255) ) return false; // Out-o-range for a byte array
    _mem[i+OFF] = (byte)res;
    return true; 
  }
  @Override boolean set8_impl(int i, double d) { return false; }
  @Override boolean hasFloat() { return _scale < 1.0; }
  @Override public AutoBuffer write(AutoBuffer bb) { return bb.putA1(_mem,_mem.length); }
  @Override public C1SChunk read(AutoBuffer bb) {
    _mem = bb.bufClose();
    _start = -1;
    _len = _mem.length-OFF;
    _scale= UDP.get8d(_mem,0);
    _bias = UDP.get4 (_mem,8);
    return this;
  }
  @Override NewChunk inflate_impl(NewChunk nc) {
    double dx = Math.log10(_scale);
    int x = (int)dx;
    if( DParseTask.pow10i(x) != _scale ) throw H2O.unimpl();
    for( int i=0; i<_len; i++ ) {
      long res = 0xFF&_mem[i+OFF];
      if( res == _NA ) nc.setInvalid(i);
      else {
        nc._ls[i] = res+_bias;
        nc._xs[i] = x;
      }
    }
    return nc;
  }
}
