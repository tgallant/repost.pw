if(typeof Math.imul == "undefined" || (Math.imul(0xffffffff,5) == 0)) {
    Math.imul = function (a, b) {
        var ah  = (a >>> 16) & 0xffff;
        var al = a & 0xffff;
        var bh  = (b >>> 16) & 0xffff;
        var bl = b & 0xffff;
        // the shift by 0 fixes the sign on the high part
        // the final |0 converts the unsigned value into a signed value
        return ((al * bl) + (((ah * bl + al * bh) << 16) >>> 0)|0);
    }
}

;(function(){
"undefined" !== typeof Math.imul && (Math.imul.a ? Math.imul.a(4294967295, 5) : Math.imul.call(null, 4294967295, 5));
function a(f) {
  var g = document.getElementById("submit");
  document.getElementById("inputForm").value = f;
  return g.click();
}
var b = ["blog_checker", "main", "redirect"], c = this;
b[0] in c || !c.execScript || c.execScript("var " + b[0]);
for (var d;b.length && (d = b.shift());) {
  var e;
  if (e = !b.length) {
    e = void 0 !== a;
  }
  e ? c[d] = a : c = c[d] ? c[d] : c[d] = {};
}
;
})();
