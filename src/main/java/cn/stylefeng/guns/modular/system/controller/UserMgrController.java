package cn.stylefeng.guns.modular.system.controller;

import cn.stylefeng.guns.config.properties.GunsProperties;
import cn.stylefeng.guns.core.common.annotion.BussinessLog;
import cn.stylefeng.guns.core.common.annotion.Permission;
import cn.stylefeng.guns.core.common.constant.Const;
import cn.stylefeng.guns.core.common.constant.dictmap.UserDict;
import cn.stylefeng.guns.core.common.constant.factory.ConstantFactory;
import cn.stylefeng.guns.core.common.constant.state.ManagerStatus;
import cn.stylefeng.guns.core.common.exception.BizExceptionEnum;
import cn.stylefeng.guns.core.log.LogObjectHolder;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.guns.core.util.ResponseVO;
import cn.stylefeng.guns.modular.system.model.User;
import cn.stylefeng.guns.modular.system.service.IUserService;
import cn.stylefeng.guns.modular.system.transfer.UserDto;
import cn.stylefeng.guns.modular.system.warpper.UserWarpper;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 系统管理员控制器
 *
 * @author fengshuonan
 * @Date 2017年1月11日 下午1:08:17
 */
@Controller
public class UserMgrController extends BaseController {

    private static String PREFIX = "/system/user/";

    @Autowired
    private GunsProperties gunsProperties;

    @Autowired
    private IUserService userService;
    
    
    /**
     * 跳转到主页
     */
    @RequestMapping(value = "/Manage", method = RequestMethod.GET)
    public String index(Model model) {

        //获取用户头像
        Integer id = ShiroKit.getUser().getId();
        User user = userService.selectById(id);
        String avatar = user.getAvatar();
        model.addAttribute("avatar", avatar);

        return "/system/index.html";
    }
    
    /**
     * 跳转到查看管理员列表的页面
     */
    @RequestMapping("/mgr")
    public String index() {
        return PREFIX + "user.html";
    }

    /**
     * 跳转到查看管理员列表的页面
     */
    @RequestMapping("/mgr/user_add")
    public String addView() {
        return PREFIX + "user_add.html";
    }

    /**
     * 跳转到角色分配页面
     */
    //@RequiresPermissions("/mgr/role_assign")  //利用shiro自带的权限检查
    @Permission
    @RequestMapping("/mgr/role_assign/{userId}")
    public String roleAssign(@PathVariable Integer userId, Model model) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = this.userService.selectOne(new EntityWrapper<User>().eq("id", userId));
        model.addAttribute("userId", userId);
        model.addAttribute("userAccount", user.getAccount());
        return PREFIX + "user_roleassign.html";
    }

    /**
     * 跳转到编辑管理员页面
     */
    @RequestMapping("/mgr/user_edit/{userId}")
    public String userEdit(@PathVariable Integer userId, Model model) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = this.userService.selectById(userId);
        model.addAttribute(user);
        //model.addAttribute("deptName", ConstantFactory.me().getDeptName(user.getDeptid()));
        LogObjectHolder.me().set(user);
        return PREFIX + "user_edit.html";
    }

    /**
     * 跳转到充值页面
     */
    @RequestMapping("/mgr/user_pay/{userId}")
    public String userPay(@PathVariable Integer userId, Model model) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = this.userService.selectById(userId);
        model.addAttribute(user);
        LogObjectHolder.me().set(user);
        return PREFIX + "user_pay.html";
    }

    /**
     * 跳转到查看用户详情页面
     */
    @RequestMapping("/mgr/user_info")
    public String userInfo(Model model) {
        Integer userId = ShiroKit.getUser().getId();
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = this.userService.selectById(userId);
        model.addAttribute(user);
        model.addAttribute("roleName", ConstantFactory.me().getRoleName(user.getRoleid()));
        //model.addAttribute("deptName", ConstantFactory.me().getDeptName(user.getDeptid()));
        LogObjectHolder.me().set(user);
        return PREFIX + "user_view.html";
    }

    /**
     * 跳转到修改密码界面
     */
    @RequestMapping("/mgr/user_chpwd")
    public String chPwd() {
        return PREFIX + "user_chpwd.html";
    }

    /**
     * 修改当前用户的密码
     */
    @RequestMapping("/mgr/changePwd")
    @ResponseBody
    public Object changePwd(@RequestParam String oldPwd, @RequestParam String newPwd, @RequestParam String rePwd) {
        if (!newPwd.equals(rePwd)) {
            throw new ServiceException(BizExceptionEnum.TWO_PWD_NOT_MATCH);
        }
        Integer userId = ShiroKit.getUser().getId();
        User user = userService.selectById(userId);
        String oldMd5 = ShiroKit.md5(oldPwd, user.getSalt());
        if (user.getPassword().equals(oldMd5)) {
            String newMd5 = ShiroKit.md5(newPwd, user.getSalt());
            user.setPassword(newMd5);
            user.updateById();
            return SUCCESS_TIP;
        } else {
            throw new ServiceException(BizExceptionEnum.OLD_PWD_NOT_RIGHT);
        }
    }

    /**
     * 查询管理员列表
     */
    @RequestMapping("/mgr/list")
    @ResponseBody
    public Object list(@Valid UserDto userDto) {
      /*  if (ShiroKit.isAdmin()) {
            List<Map<String, Object>> users = userService.selectUsers(null, name, beginTime, endTime, deptid);
            return new UserWarpper(users).wrap();
        } else {
            DataScope dataScope = new DataScope(ShiroKit.getDeptDataScope());
            List<Map<String, Object>> users = userService.selectUsers(dataScope, name, beginTime, endTime, deptid);
            return new UserWarpper(users).wrap();
        }*/
    	
        List<Map<String, Object>> users = userService.selectUsers(null, userDto);
        return new UserWarpper(users).wrap();
    }

    /**
     * 添加管理员
     */
    @RequestMapping("/mgr/add")
    @BussinessLog(value = "添加用户", key = "account", dict = UserDict.class)
    @ResponseBody
    public ResponseData add(@Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        
        if (user!=null && user.getId()!=null) {
        	this.userService.updateUser(user);
		}else {
			// 判断账号是否重复
		    User theUser = userService.getByAccount(user.getAccount());
	        if (theUser != null) {
	            throw new ServiceException(BizExceptionEnum.USER_ALREADY_REG);
	        }
	        user.setSalt(ShiroKit.getRandomSalt(5));
	        user.setPassword(ShiroKit.md5(user.getPassword(), user.getSalt()));
	        user.setStatus(ManagerStatus.OK.getCode());
	        user.setCreatetime(new Date());
	        user.setRoleid("0"); //虚拟用户
	        this.userService.insert(user);
        }
        return SUCCESS_TIP;
    }
    
    /**
     * 修改用户
     *
     */
    @RequestMapping("/updateUser")
    @BussinessLog(value = "修改用户", key = "account", dict = UserDict.class)
    @ResponseBody
    public ResponseVO updateUser(@Valid UserDto userDto) {
        //User oldUser = userService.selectById(user.getId());
       
        // assertAuth(user.getId());
        // ShiroUser shiroUser = ShiroKit.getUser();
        User oldUser = userService.selectById(userDto.getId());
        // 判断是否修改了密码
        if (StringUtils.isNotEmpty(userDto.getPassword())) {
            oldUser.setPassword(ShiroKit.md5(oldUser.getPassword(), oldUser.getSalt()));
        }
        oldUser.setName(userDto.getName());
        oldUser.setNickName(userDto.getNickName());
        oldUser.setRoleid(userDto.getRoleid());
        oldUser.setPhone(userDto.getPhone());
        oldUser.setEmail(userDto.getEmail());
        oldUser.setCardNo(userDto.getCardNo());

        this.userService.updateById(oldUser);
        return new ResponseVO(0);
        
    }

    /**
     * 修改用户余额
     *
     */
    @RequestMapping("/updateUserBalance")
    @BussinessLog(value = "修改用户余额", key = "account", dict = UserDict.class)
    @ResponseBody
    public ResponseVO updateUserBalance(@Valid UserDto userDto) {

        User oldUser = userService.selectById(userDto.getId());

        oldUser.setBalance(oldUser.getBalance().add(userDto.getPaySum())); // 更新余额
        oldUser.setPaySum(oldUser.getPaySum().add(userDto.getPaySum())); // 充值金额

        this.userService.updateById(oldUser);
        return new ResponseVO(0);

    }

    /**
     * 删除
     *
     * @param userId  用户id
     * @param type    =1启动 =3删除
     * @return
     */
    @RequestMapping("/mgr/delete")
    @BussinessLog(value = "删除管理员", key = "userId", dict = UserDict.class)
    @ResponseBody
    public ResponseData delete(@RequestParam Integer userId, @RequestParam Integer type) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能删除超级管理员
        if (userId.equals(Const.ADMIN_ID)) {
            throw new ServiceException(BizExceptionEnum.CANT_DELETE_ADMIN);
        }
        assertAuth(userId);

        int flag = ManagerStatus.DELETED.getCode();
        if (null != type && type == 1) {
            flag = ManagerStatus.OK.getCode();
        }

        this.userService.setStatus(userId, flag);
        return SUCCESS_TIP;
    }

    /**
     * 查看管理员详情
     */
    @RequestMapping("/mgr/view/{userId}")
    @ResponseBody
    public User view(@PathVariable Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        return this.userService.selectById(userId);
    }

    /**
     * 重置管理员的密码
     */
    @RequestMapping("/mgr/reset")
    @BussinessLog(value = "重置管理员密码", key = "userId", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData reset(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        User user = this.userService.selectById(userId);
        user.setSalt(ShiroKit.getRandomSalt(5));
        user.setPassword(ShiroKit.md5(Const.DEFAULT_PWD, user.getSalt()));
        this.userService.updateById(user);
        return SUCCESS_TIP;
    }

    /**
     * 冻结用户
     */
    @RequestMapping("/mgr/freeze")
    @BussinessLog(value = "冻结用户", key = "userId", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData freeze(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能冻结超级管理员
        if (userId.equals(Const.ADMIN_ID)) {
            throw new ServiceException(BizExceptionEnum.CANT_FREEZE_ADMIN);
        }
        assertAuth(userId);
        this.userService.setStatus(userId, ManagerStatus.FREEZED.getCode());
        return SUCCESS_TIP;
    }

    /**
     * 解除冻结用户
     */
    @RequestMapping("/mgr/unfreeze")
    @BussinessLog(value = "解除冻结用户", key = "userId", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData unfreeze(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        this.userService.setStatus(userId, ManagerStatus.OK.getCode());
        return SUCCESS_TIP;
    }

    /**
     * 分配角色
     */
    @RequestMapping("/mgr/setRole")
    @BussinessLog(value = "分配角色", key = "userId,roleIds", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData setRole(@RequestParam("userId") Integer userId, @RequestParam("roleIds") String roleIds) {
        if (ToolUtil.isOneEmpty(userId, roleIds)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能修改超级管理员
        if (userId.equals(Const.ADMIN_ID)) {
            throw new ServiceException(BizExceptionEnum.CANT_CHANGE_ADMIN);
        }
        assertAuth(userId);
        this.userService.setRoles(userId, roleIds);
        return SUCCESS_TIP;
    }

    /**
     * 上传图片
     */
    @RequestMapping(method = RequestMethod.POST, path = "/mgr/upload")
    @ResponseBody
    public String upload(@RequestPart("file") MultipartFile picture) {

        String pictureName = UUID.randomUUID().toString() + "." + ToolUtil.getFileSuffix(picture.getOriginalFilename());
        try {
            String fileSavePath = gunsProperties.getFileUploadPath();
            picture.transferTo(new File(fileSavePath + pictureName));
        } catch (Exception e) {
            throw new ServiceException(BizExceptionEnum.UPLOAD_ERROR);
        }
        return pictureName;
    }

    /**
     * 判断当前登录的用户是否有操作这个用户的权限
     */
    private void assertAuth(Integer userId) {
        /*if (ShiroKit.isAdmin()) {
            return;
        }
        List<Integer> deptDataScope = ShiroKit.getDeptDataScope();
        User user = this.userService.selectById(userId);
        Integer deptid = Integer.valueOf(user.getDeptid());
        if (deptDataScope.contains(deptid)) {
            return;
        } else {
            throw new ServiceException(BizExceptionEnum.NO_PERMITION);
        }*/
    }
}