package cassius;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.Charset;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;


public class ExtractCodes
{
   // https://invitecodematrix.com/content/turf-wars&page=50
   final static String CODE_URL = "https://invitecodematrix.com/content/turf-wars";
   final static String TEMP_CODE_URL = "https://invitecodematrix.com/content/turf-wars-temp-codes";
   final static String USER_URL = "https://invitecodematrix.com/user";
   final static String LOGOUT_URL = "https://invitecodematrix.com/logout";
   final static String PAGE_PARAM = "page=";
   final static Charset charset = Charset.forName("US-ASCII");
   
   enum CodesPerPageOption
   {
      _25(25), _50(50), _100(100);
      private int value;

      private CodesPerPageOption(int value)
      {
         this.value = value;
      }
      
      public String getValue()
      {
         return Integer.toString(value);
      }
   };
   
   WebDriver driver;

   String mUserName;
   String mPassword;
   String mOutputFileName;
   int mStartPage = -1;
   int mEndPage = -1;
   

   public ExtractCodes()
   {
      super();
   }

   public boolean initialize(String[] args)
   {
      int i = 0;
      String arg;
      String error = null;

      while (i < args.length && args[i].startsWith("-"))
      {
         arg = args[i++];

         if (arg.equals("-l"))
         {
            if (i < args.length)
               mUserName = args[i++];
            else
               error = "-l requires a username.";
         }
         else if (arg.equals("-p"))
         {
            if (i < args.length)
               mPassword = args[i++];
            else
               error = "-p requires a password.";
         }
         else if (arg.equals("-output"))
         {
            if (i < args.length)
               mOutputFileName = args[i++];
            else
               error = "-output requires a filename";
         }
         else if (arg.equals("-start"))
         {
            if (i < args.length)
            {
               String value = args[i++];
               try
               {
                  mStartPage = Integer.parseInt(value);
               }
               catch (NumberFormatException ex)
               {
                  error = "Invalid number '" + value + "' for -start.";
               }
            }
            else
               error = "-start requires a number";
         }
         else if (arg.equals("-end"))
         {
            if (i < args.length)
               {
                  String value = args[i++];
                  try
                  {
                     mEndPage = Integer.parseInt(value);
                  }
                  catch (NumberFormatException ex)
                  {
                     error = "Invalid number '" + value + "' for -end.";
                  }

                  if (mEndPage > 100)
                  {
                     error = "Invalid end page " + mEndPage + ". Maximum 100.";
                  }
               }
            else
               error = "-end requires a number";
         }
         
         if (error != null)
         {
            break;
         }
      }
      
/*
  if (error == null && i == args.length)
         error = "Usage: ExtractCodes  [-l] username [-p] password [-output afile] filename";
 */     
      if (error != null)
      {
         logError(error);
         return false;
      }
      
      // Default start page is the first page
      if (mStartPage < 0)
      {
         mStartPage = 0;
      }
      
      // Default end page is 10 page down
      if (mEndPage < 0)
      {
         mEndPage = mStartPage + 10;
      }

      // If a filename was not specified, use output.txt in current directory.
      if (mOutputFileName == null)
      {
         mOutputFileName = System.getProperty("user.dir") + File.separator + "output.txt";
      }
      
      return true;
   }

   public void getTempCode()
   {
      // Load temp code page
      driver.get(TEMP_CODE_URL);
      
      setCodePerPage(CodesPerPageOption._100);
      
   }
   
   /**
    * 
    */
   public void getCode()
   {
      // Load code page
      driver.get(CODE_URL);

      initializeSearch();

      System.out.println("Writing to file: " + mOutputFileName);
      
      int start;
      if (mStartPage == 0)
      {
         System.out.println("Grabing first page");
         grabAndSave();
         start = 2;
      }
      else
      {
         start = mStartPage;
      }

      System.out.println("Grabing code from page " + start + " to page " + mEndPage);

      for (int page = start; page <= mEndPage; page++)
      {
         String url = CODE_URL + "&" + PAGE_PARAM + page;

         driver.get(url);

         System.out.println("Grabing page " + page);
         grabAndSave();
      }
   }

   protected void grabAndSave()
   {
      BufferedWriter writer = null;

      try
      {
         writer = new BufferedWriter(new FileWriter(mOutputFileName, true));

         // cpbutton
         WebElement element = driver.findElement(By.id("cpbutton"));
         element.click();

         // cp_content
         element = driver.findElement(By.id("cp_content"));
         
         // Remove the text of the Close button
         String codes = element.getText();
         codes = codes.substring(codes.indexOf('\n')+1);

         //         String[] codesArray = codes.split("\\s");
         //         System.out.println(codesArray.length + "codes.");

         writer.append(codes);
         writer.newLine();
      }
      catch (IOException ex)
      {
         logError(ex.getMessage());
      }
      finally
      {
         try
         {
            if (writer != null)
            {
               writer.close();
            }
         }
         catch (IOException ex)
         {
            logError(ex.getMessage());
         }

      }
   }

   /**
    * 
    */
   protected void setCodePerPage(CodesPerPageOption perPage)
   {
      // perpage
      Select droplist = new Select(driver.findElement(By.name("perpage")));
      droplist.selectByVisibleText(perPage.getValue());
   }
   
   /**
    * 
    */
   protected void initializeSearch()
   {
      setCodePerPage(CodesPerPageOption._100);

      // filtersbutton
      WebElement element = driver.findElement(By.id("filtersbutton"));
      element.click();

      // listfilter
      Select droplist = new Select(driver.findElement(By.id("listfilter")));
      droplist.selectByValue("s");

      // filter_list
      element = driver.findElement(By.name("filter_list"));
      element.submit();
   }
   
   protected void logError(String error)
   {
      System.out.println("ERROR: " + error);
   }
   
   public void start()
   {
      // Create a new instance of the Firefox driver
      driver = new FirefoxDriver();
   }

   public void end()
   {
      //Close the browser
      if (driver != null)
      {
         driver.quit();
      }
   }
   
   public boolean login()
   {
      if (mUserName == null || mPassword == null)
      {
         System.out.println("Missing username or password.");
         return false;
      }
      
      // Load the login page
      driver.get(USER_URL);
      
      // Find the text input element by its name
      WebElement element = driver.findElement(By.id("edit-name"));
      // Enter username
      element.sendKeys(mUserName);
      // Enter password
      WebElement passwordElem = driver.findElement(By.id("edit-pass"));
      passwordElem.sendKeys(mPassword);

      // Now submit the form. WebDriver will find the form for us from the element
      element.submit();

      // Check the login process was successful
      List<WebElement> userElems = driver.findElements(By.className("your_account"));
      if (userElems.size() > 0)
      {
         String content = userElems.get(0).getText();
         if (content != null && content.startsWith(mUserName))
         {
            System.out.println("Successful login.");
            return true;
         }
      }
      else
      {
         // div class="messages error"
         // Sorry, unrecognized username or password.
         logError("Cannot login user " + mUserName);
      }
      
      return false;
   }
   
   public void logout()
   {
      driver.get(LOGOUT_URL);

      // If logout successful, the login class should be present
      List<WebElement> userElems = driver.findElements(By.className("login"));
      if (userElems.size() > 0)
      {
         String content = userElems.get(0).getText();
         if (content != null && content.startsWith("Login"))
         {
            System.out.println("Logout successful.");
         }
      }
   }
   
   public static void main(String[] args)
   {
      ExtractCodes extract = new ExtractCodes();
      
      if (!extract.initialize(args))
      {
         return;
      }
      
      try
      {
         extract.start();
         
         if (extract.login())
         {
            extract.getCode();

            extract.logout();
         }
      }
      catch (Exception ex)
      {
         System.out.println("Error during execution: " + ex.getMessage());
      }
      finally
      {
         extract.end();
      }
      

      // Google's search is rendered dynamically with JavaScript.
      // Wait for the page to load, timeout after 10 seconds
      /*
      (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>()
      {
         public Boolean apply(WebDriver d)
         {
            return d.getTitle().toLowerCase().startsWith("Invite Code Matrix");
         }
      });
      System.out.println("Page title is: " + driver.getTitle());
   */

      System.out.println("Done.");
   }
}
