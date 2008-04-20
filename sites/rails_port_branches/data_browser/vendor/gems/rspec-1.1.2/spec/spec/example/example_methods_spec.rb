require File.dirname(__FILE__) + '/../../spec_helper'

module Spec
  module Example
    module ModuleThatIsReopened
    end

    module ExampleMethods
      include ModuleThatIsReopened
    end

    module ModuleThatIsReopened
      def module_that_is_reopened_method
      end
    end

    describe "ExampleMethods with an included module that is reopened" do
      it "should have repoened methods" do
        method(:module_that_is_reopened_method).should_not be_nil
      end
    end

    describe ExampleMethods, "lifecycle" do
      before do
        @options = ::Spec::Runner::Options.new(StringIO.new, StringIO.new)
        @options.formatters << mock("formatter", :null_object => true)
        @options.backtrace_tweaker = mock("backtrace_tweaker", :null_object => true)
        @reporter = FakeReporter.new(@options)
        @options.reporter = @reporter

        ExampleMethods.before_all_parts.should == []
        ExampleMethods.before_each_parts.should == []
        ExampleMethods.after_each_parts.should == []
        ExampleMethods.after_all_parts.should == []
        def ExampleMethods.count
          @count ||= 0
          @count = @count + 1
          @count
        end
      end

      after do
        ExampleMethods.instance_variable_set("@before_all_parts", [])
        ExampleMethods.instance_variable_set("@before_each_parts", [])
        ExampleMethods.instance_variable_set("@after_each_parts", [])
        ExampleMethods.instance_variable_set("@after_all_parts", [])
      end

      it "should pass before and after callbacks to all ExampleGroup subclasses" do
        ExampleMethods.before(:all) do
          ExampleMethods.count.should == 1
        end

        ExampleMethods.before(:each) do
          ExampleMethods.count.should == 2
        end

        ExampleMethods.after(:each) do
          ExampleMethods.count.should == 3
        end

        ExampleMethods.after(:all) do
          ExampleMethods.count.should == 4
        end

        @example_group = Class.new(ExampleGroup) do
          it "should use ExampleMethods callbacks" do
          end
        end
        @example_group.run
        ExampleMethods.count.should == 5
      end
      
      describe "run_with_description_capturing" do
        before(:each) do
          @example_group = Class.new(ExampleGroup) do end
          @example = @example_group.new("foo", &(lambda { 2.should == 2 }))
          @example.run_with_description_capturing
        end
      
        it "should provide the generated description" do
          @example.instance_eval { @_matcher_description }.should == "should == 2"
        end
      
        it "should clear the global generated_description" do
          Spec::Matchers.generated_description.should == nil
        end
      end
    end
  end
end